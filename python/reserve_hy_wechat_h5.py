import os
import re
import sys
from dataclasses import dataclass
from typing import Dict, Optional, Tuple
from pathlib import Path
import subprocess
import json
import time
import traceback
from datetime import datetime, timedelta

import requests
from bs4 import BeautifulSoup


BASE = "http://hy.weiyouyuan.com.cn"
CONFIG_PATH = Path(__file__).resolve().parents[1] / "config" / "user-config.json"

def _ts() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def log_info(msg: str) -> None:
    print(f"[{_ts()}] [INFO] {msg}", flush=True)


def log_warn(msg: str) -> None:
    print(f"[{_ts()}] [WARN] {msg}", flush=True)


def log_error(msg: str) -> None:
    print(f"[{_ts()}] [ERROR] {msg}", flush=True)


def format_exc(e: BaseException) -> str:
    return "".join(traceback.format_exception(type(e), e, e.__traceback__)).strip()


def load_user_config() -> dict:
    """
    从 config/user-config.json 读取用户配置（若不存在则返回空 dict）。
    """
    if not CONFIG_PATH.exists():
        return {}
    try:
        with open(CONFIG_PATH, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        log_warn(f"读取用户配置失败，将使用代码内默认配置。详情: {format_exc(e)}")
        return {}

@dataclass
class WeChatSessionConfig:
    """
    微信 H5 会话配置：这里直接写死你自己的信息，
    以后运行脚本无需再配置环境变量。
    """

    # 抓包得到的 UA/Referer/Cookie
    user_agent: str = (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        "(KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 NetType/WIFI "
        "MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090a13) "
        "UnifiedPCWindowsWechat(0xf2541113) XWEB/18163 Flue"
    )
    referer: str = (
        "http://hy.weiyouyuan.com.cn/mobile/second.aspx?"
        "cgid=5&ydxmid=2&days=03/16&select_id=20_1&rand=0.6890311109581895"
    )
    cookie: str = (
        "hy_openid=ormHy6O_HrttxfiT8bKRZmzOgJc8; "
        "ASP.NET_SessionId=ocw5uy2plui3pop2n0dtmg4z"
    )

    # 预约人信息（手机号 / 姓名 / 学号）
    tel: str = "18436900381"
    realname: str = "刁越洋"
    sex: str = "男"
    userno: str = "2107210303"
    yy_number: str = ""       # 预约人数字段，可为空
    tx_realname: str = ""     # 同行人姓名，可为空

    # 场馆/项目/场地/日期配置
    cgid: str = "5"           # 场馆 ID（示例：5 = 东体育馆）
    ydxmid: str = "1"         # 项目 ID（示例：1=羽毛球, 2=乒乓球，具体以抓包为准）
    cdid: str = "20_1"        # 场地+时间段 ID（sd_cdid，例如 20_1 = 20 点第 1 号场）
    days: str = "AUTO_TODAY"  # 日期："AUTO_TODAY"=始终使用当天，或手动写成 "03/16"


def build_session(cfg: WeChatSessionConfig) -> requests.Session:
    s = requests.Session()
    s.headers.update(
        {
            "User-Agent": cfg.user_agent,
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/wxpic,image/webp,image/apng,*/*;q=0.8",
            "Accept-Language": "zh-CN,zh;q=0.9",
            "Accept-Encoding": "gzip, deflate",
            "Connection": "keep-alive",
            "Upgrade-Insecure-Requests": "1",
            "Referer": cfg.referer,
            "Cookie": cfg.cookie,
        }
    )
    return s


def http_get(session: requests.Session, url: str, *, timeout: int = 20) -> requests.Response:
    t0 = time.perf_counter()
    log_info(f"HTTP GET {url}")
    try:
        r = session.get(url, timeout=timeout)
    except Exception as e:
        log_error(f"GET 失败: {url}\n{format_exc(e)}")
        raise
    dt = (time.perf_counter() - t0) * 1000
    log_info(f"GET 完成: status={r.status_code} elapsed_ms={dt:.1f}")
    return r


def http_post(
    session: requests.Session,
    url: str,
    *,
    data: dict,
    timeout: int = 20,
    log_request: bool = True,
) -> requests.Response:
    t0 = time.perf_counter()
    if log_request:
        log_info(f"HTTP POST {url} (form_keys={','.join(list(data.keys()))})")
    try:
        r = session.post(url, data=data, timeout=timeout)
    except Exception as e:
        log_error(f"POST 失败: {url}\n{format_exc(e)}")
        raise
    dt = (time.perf_counter() - t0) * 1000
    body_preview = (r.text or "")[:200].replace("\n", " ")
    if log_request:
        log_info(f"POST 完成: status={r.status_code} elapsed_ms={dt:.1f} body_preview={body_preview}")
    return r


def http_post_json(
    session: requests.Session,
    url: str,
    *,
    data: dict,
    timeout: int = 20,
    log_request: bool = True,
) -> dict:
    r = http_post(session, url, data=data, timeout=timeout, log_request=log_request)
    try:
        return r.json()
    except Exception as e:
        log_error(f"JSON 解析失败: url={url}\nbody_preview={(r.text or '')[:300]}\n{format_exc(e)}")
        raise


def extract_webforms_state(html: str) -> Dict[str, str]:
    """
    ASP.NET WebForms 页面通常依赖隐藏字段维持状态：
    __VIEWSTATE / __EVENTVALIDATION / __VIEWSTATEGENERATOR 等。
    """
    # 兼容：在 Windows/网络环境下 lxml 可能安装失败，先用内置解析器即可跑通流程
    soup = BeautifulSoup(html, "html.parser")
    data: Dict[str, str] = {}
    for k in [
        "__VIEWSTATE",
        "__VIEWSTATEGENERATOR",
        "__EVENTVALIDATION",
        "__EVENTTARGET",
        "__EVENTARGUMENT",
    ]:
        el = soup.find("input", {"name": k})
        if el and el.get("value") is not None:
            data[k] = el["value"]
    return data


def download_captcha_image(session: requests.Session, captcha_url: str) -> bytes:
    """
    下载验证码图片（GIF）。
    本项目当前不做 OCR/CNN 识别，验证码通过 0-20 穷举解决。
    """
    r = http_get(session, captcha_url, timeout=15)
    r.raise_for_status()
    return r.content


def recognize_captcha_by_local_cnn(image_path: str) -> Tuple[Optional[int], str]:
    """
    优先使用项目自带的 CNN（captcha/predict.py + checkpoints/best.pt），
    避免依赖系统安装 tesseract。
    """
    repo_root = Path(__file__).resolve().parents[1]
    predict_py = repo_root / "captcha" / "predict.py"
    model_pt = repo_root / "captcha" / "checkpoints" / "best.pt"
    if not predict_py.exists():
        return None, f"CNN_UNAVAILABLE: missing {predict_py}"
    if not model_pt.exists():
        return None, f"CNN_UNAVAILABLE: missing {model_pt}"

    try:
        p = subprocess.run(
            [sys.executable, str(predict_py), str(image_path), "--model", str(model_pt)],
            cwd=str(repo_root),
            capture_output=True,
            text=True,
            timeout=20,
        )
        if p.returncode != 0:
            return None, f"CNN_ERROR: {p.stderr.strip() or p.stdout.strip()}"
        out = (p.stdout or "").strip()
        try:
            return int(out), f"CNN_OK: {out}"
        except Exception:
            return None, f"CNN_BAD_OUTPUT: {out}"
    except Exception as e:
        return None, f"CNN_EXCEPTION: {e}"


def submit_save_yy_data(
    session: requests.Session,
    *,
    cfg: WeChatSessionConfig,
    yzm_value: int,
    rand: Optional[float] = None,
    log_request: bool = True,
) -> Tuple[int, str, str]:
    """
    提交预约（save_yy_data.ashx），返回 (http_status, state, id)
    """
    form = {
        "tel": cfg.tel,
        "realname": cfg.realname,
        "sex": cfg.sex,
        "yy_number": cfg.yy_number,
        "userno": cfg.userno,
        "tx_realname": cfg.tx_realname,
        "cgid": cfg.cgid,
        "ydxmid": cfg.ydxmid,
        "cdid": cfg.cdid,
        "days": cfg.days,
        "openid": cfg.cookie.split("hy_openid=")[-1].split(";")[0],
        "yzm": str(yzm_value),
    }

    if rand is None:
        rand = time.time() % 1  # 保持类似 rand=0.x 的形式
    save_url = f"{BASE}/mobile/save_yy_data.ashx?rand={rand}"
    resp = http_post(session, save_url, data=form, timeout=20, log_request=log_request)
    http_status = resp.status_code

    state = ""
    rid = ""
    try:
        j = resp.json()
        state = str(j.get("state", ""))
        rid = str(j.get("id", ""))
    except Exception:
        # 非 JSON 的话尽量从文本里挖一下
        t = (resp.text or "").strip()
        try:
            j = json.loads(t)
            state = str(j.get("state", ""))
            rid = str(j.get("id", ""))
        except Exception:
            state = "NON_JSON"
            rid = ""
    return http_status, state, rid


def solve_captcha_by_bruteforce_0_20(
    session: requests.Session, *, cfg: WeChatSessionConfig
) -> Tuple[Optional[int], int, str, str, str]:
    """
    彻底绕开识别：验证码答案范围是 0-20。
    调用方需提前 GET 一次 ValidCode.aspx 生成验证码（写入 Session），然后在 0..20 中尝试直到成功。

    注意：
    - 这会产生最多 21 次请求
    - 若站点对连续错误有封禁/验证码失效策略，可把 interval_ms 调大或每次重取验证码
    """
    interval_ms = 80
    last_states = []
    for v in range(0, 21):
        # 穷举阶段不刷屏：不打印每一次 POST 的详细日志，只统计次数
        http_status, state, rid = submit_save_yy_data(
            session, cfg=cfg, yzm_value=v, log_request=False
        )
        last_states.append((v, http_status, state, rid))

        attempts = len(last_states)

        # 成功判定：state=1 且 id 非空（与前端 JS 一致）
        if state == "1" and rid and rid != "0":
            return v, attempts, state, rid, f"BRUTE_OK: attempts={attempts}/21 yzm={v} state={state} id={rid}"

        # 经验判定：有些实现成功时 state=0/true 且带 id；这里保守，只要 state 看起来不是“验证码错误”就停止交给上层处理
        if state not in {"40", "NON_JSON"}:
            return v, attempts, state, rid, f"BRUTE_STOP: attempts={attempts}/21 yzm={v} state={state} id={rid}"

        time.sleep(interval_ms / 1000.0)

    return None, 21, "NON_JSON", "", "BRUTE_FAIL: attempts=21/21 last=" + str(last_states[-3:])


def sd_to_timerange(sd: int) -> str:
    """
    页面 sd 与时间段的常见对应：sd=7 表示 07:00-08:00 ... sd=22 表示 22:00-23:00
    """
    if sd < 0 or sd > 23:
        return f"sd={sd}"
    start = sd
    end = sd + 1
    return f"{start:02d}:00-{end:02d}:00"


def fetch_index_1_html(session: requests.Session) -> str:
    """
    场次表格页（含各格子 div id，如 20_1）
    """
    url = f"{BASE}/mobile/index_1.aspx"
    r = http_get(session, url, timeout=20)
    r.raise_for_status()
    if not r.encoding or r.encoding.lower() in {"iso-8859-1", "ascii"}:
        r.encoding = r.apparent_encoding
    return r.text


def parse_time_labels_from_index_1(html: str) -> dict:
    """
    从 index_1.aspx 页面解析 sd(时段编号) -> 文本时间段（如 07:00-08:00）
    """
    soup = BeautifulSoup(html, "html.parser")
    # 左侧时间列是一个独立表格，里面 td 多为 “07:00-08:00”
    labels = {}
    # 粗略：找所有看起来像 “07:00-08:00” 的 td 文本，按出现顺序对应 sd=7..22
    tds = [td.get_text(strip=True) for td in soup.find_all("td")]
    times = [t for t in tds if re.match(r"^\d{2}:\d{2}\-\d{2}:\d{2}$", t)]
    # 页面默认时段是 07-23（与你 README HOURS 一致），sd 也通常是 7..22
    sd = 7
    for t in times:
        if sd > 22:
            break
        labels[str(sd)] = t
        sd += 1
    return labels


def parse_court_labels_from_index_1(html: str) -> dict:
    """
    解析 场地编号(1/2/3/4...) -> 表头文本（如 “1号场”）
    """
    soup = BeautifulSoup(html, "html.parser")
    ths = [th.get_text(strip=True) for th in soup.find_all("th")]
    courts = {}
    for t in ths:
        m = re.match(r"^(\d+)\s*号场$", t)
        if m:
            courts[m.group(1)] = t
    return courts


def list_available_cells(
    session: requests.Session,
    *,
    cgid: str,
    ydxmid: str,
    days: str,
    log_request: bool = True,
) -> list:
    """
    调用 get_kyy_list.ashx 获取“可预约”列表，返回 [{select_id, sd, cdid, ygq}]

    select_id 就是页面/提交用的 cdid 参数（例如 20_1）。
    """
    url = f"{BASE}/mobile/get_kyy_list.ashx?rand={time.time() % 1}"
    payload = {"cgid": cgid, "ydxmid": ydxmid, "days": days}
    j = http_post_json(session, url, data=payload, timeout=20, log_request=log_request)
    yysd_list = j.get("yysd_list") or []
    out = []
    for item in yysd_list:
        sd = str(item.get("sd", "")).strip()
        cdid = str(item.get("cdid", "")).strip()
        ygq = str(item.get("ygq", "")).strip()
        if not sd or not cdid:
            continue
        out.append(
            {
                "select_id": f"{sd}_{cdid}",
                "sd": sd,
                "cdid": cdid,
                "ygq": ygq,
            }
        )
    return out


def list_booked_cells(
    session: requests.Session,
    *,
    cgid: str,
    ydxmid: str,
    days: str,
    log_request: bool = True,
) -> list:
    """
    调用 get_yyy_list.ashx 获取“已预约(橙色)”列表，返回 [{select_id, sd, cdid}]
    """
    url = f"{BASE}/mobile/get_yyy_list.ashx?rand={time.time() % 1}"
    payload = {"cgid": cgid, "ydxmid": ydxmid, "days": days}
    j = http_post_json(session, url, data=payload, timeout=20, log_request=log_request)
    yysd_list = j.get("yysd_list") or []
    out = []
    for item in yysd_list:
        sd = str(item.get("sd", "")).strip()
        cdid = str(item.get("cdid", "")).strip()
        if not sd or not cdid:
            continue
        out.append({"select_id": f"{sd}_{cdid}", "sd": sd, "cdid": cdid})
    return out


def parse_all_cell_ids_from_index_1(html: str) -> set:
    """
    从 index_1.aspx 解析所有格子的 DOM id（格式：sd_cdid，例如 20_1）。
    页面里格子通常是 <div class="div_grey" id="7_1" ...>不可约</div>
    """
    soup = BeautifulSoup(html, "html.parser")
    all_ids = set()
    for div in soup.find_all("div"):
        did = (div.get("id") or "").strip()
        if not did:
            continue
        if re.match(r"^\d+_\d+$", did):
            all_ids.add(did)
    return all_ids


def choose_best_select_id(
    *,
    avail_items: list,
    booked_ids: set,
    prefer_sd: list,
    exclude_ids: Optional[set] = None,
) -> Optional[str]:
    """
    从可预约列表中选择一个最优格子：
    - 橙色优先覆盖：如果在 booked_ids 中则跳过
    - 时段优先级：prefer_sd（例如 [20, 18]）
    - 同时段下：优先选择场地号 cdid 数字更小的
    """
    # avail_items: [{select_id, sd, cdid, ygq}]
    candidates = []
    exclude_ids = exclude_ids or set()
    for it in avail_items:
        sid = it.get("select_id")
        if not sid or sid in booked_ids or sid in exclude_ids:
            continue
        try:
            sd = int(it.get("sd"))
        except Exception:
            continue
        try:
            court = int(it.get("cdid"))
        except Exception:
            court = 9999
        candidates.append((sd, court, sid))

    if not candidates:
        return None

    # 按 prefer_sd 过滤选择
    for target_sd in prefer_sd:
        same = [c for c in candidates if c[0] == int(target_sd)]
        if same:
            same.sort(key=lambda x: (x[1], x[2]))
            return same[0][2]

    # fallback：任何可用格子里选场地号最小
    candidates.sort(key=lambda x: (x[0], x[1], x[2]))
    return candidates[0][2]


def attempt_one_cell(
    session: requests.Session,
    *,
    cfg: WeChatSessionConfig,
    base_referer: str,
    select_id: str,
) -> Tuple[bool, str, str]:
    """
    尝试抢一个格子（select_id=sd_cdid）。
    返回 (success, state, id)
    """
    cfg.cdid = select_id
    cfg.referer = update_referer_select_id(base_referer, select_id)
    session.headers["Referer"] = cfg.referer

    # 打开填写页面（保持流程一致）
    r = http_get(session, cfg.referer, timeout=20)
    r.raise_for_status()

    # 生成验证码（只 GET 一次）
    img_bytes = download_captcha_image(session, f"{BASE}/yzm/ValidCode.aspx")

    # 穷举验证码
    yzm_value, attempts, brute_state, brute_id, msg = solve_captcha_by_bruteforce_0_20(session, cfg=cfg)
    log_info(f"captcha_solve={msg}")

    # 成功即停止（以穷举阶段为准）
    if brute_state == "1" and brute_id:
        try:
            out = os.getenv("HY_CAPTCHA_OUT", "captcha.gif")
            with open(out, "wb") as f:
                f.write(img_bytes)
            log_info(f"captcha_saved={out}")
        except Exception as e:
            log_warn(f"保存验证码图片失败（不影响预约结果）：{format_exc(e)}")
        return True, brute_state, brute_id

    # 失败：也落盘一次验证码，便于排查（但放在失败末尾，不阻塞主逻辑太多）
    try:
        out = os.getenv("HY_CAPTCHA_OUT", "captcha.gif")
        with open(out, "wb") as f:
            f.write(img_bytes)
    except Exception:
        pass
    return False, brute_state, brute_id


def update_referer_select_id(referer: str, select_id: str) -> str:
    """
    second.aspx 的 referer 通常带 select_id=xx_x。
    这里把它替换成新的 select_id，保持 referer 与提交一致。
    """
    if "select_id=" in referer:
        # 注意：替换串若以数字开头，会与 \1 产生歧义（如 \120 被解释为 120 号分组）。
        # 用函数替换或 \g<1> 规避该问题。
        return re.sub(r"(select_id=)[^&]+", lambda m: m.group(1) + select_id, referer)
    # 若 referer 没带，直接追加
    joiner = "&" if "?" in referer else "?"
    return f"{referer}{joiner}select_id={select_id}"


def update_referer_days(referer: str, days: str) -> str:
    """
    替换 second.aspx 链接中的 days=MM/DD 为指定日期。
    """
    if "days=" in referer:
        return re.sub(r"(days=)[^&]+", lambda m: m.group(1) + days.replace("/", "%2F"), referer)
    joiner = "&" if "?" in referer else "?"
    return f"{referer}{joiner}days={days.replace('/', '%2F')}"

def main() -> int:
    """
    先验证：用你抓包的 UA/Referer/Cookie 在 PC 端访问 second.aspx，能否拿到正确页面。
    后续拿到“提交预约”的抓包后，把 POST 参数按同样模式补齐即可全自动。
    """
    t0 = time.perf_counter()
    log_info("程序启动")

    # 可选：在抢号前阻塞等待到北京时间早上 6:00（本地时间假设已设置为东八区）
    if len(sys.argv) >= 2 and sys.argv[1].lower() in {"wait6", "at6"}:
        now = datetime.now()
        target = now.replace(hour=6, minute=0, second=0, microsecond=0)
        if now >= target:
            # 已经过了今天 6 点，等到明天 6 点
            target = target + timedelta(days=1)

        wait_seconds = (target - now).total_seconds()
        log_info(
            f"启用 6 点定时：当前时间={now.strftime('%Y-%m-%d %H:%M:%S')}，"
            f"将等待 {int(wait_seconds)} 秒在 {target.strftime('%Y-%m-%d %H:%M:%S')} 开始抢号"
        )

        # 更精确的等待：每次重新计算剩余时间，最后 1 秒细粒度 sleep
        while True:
            remaining = (target - datetime.now()).total_seconds()
            if remaining <= 0:
                break
            if remaining > 60:
                time.sleep(30)
            elif remaining > 5:
                time.sleep(1)
            elif remaining > 1:
                time.sleep(0.2)
            else:
                time.sleep(0.02)

        log_info("已到 6 点，开始执行抢号逻辑。")
    try:
        cfg = WeChatSessionConfig()

        # 若存在外部 JSON 配置，则用其覆盖默认字段
        user_conf = load_user_config()
        if user_conf:
            for k in [
                "tel",
                "realname",
                "sex",
                "userno",
                "yy_number",
                "tx_realname",
                "cgid",
                "ydxmid",
                "cdid",
                "days",
            ]:
                if k in user_conf and getattr(cfg, k, None) is not None:
                    setattr(cfg, k, str(user_conf[k]))

        # 自动处理 days=AUTO_TODAY：始终抢“当天”的场次（格式 MM/DD）
        if cfg.days.upper() == "AUTO_TODAY":
            today_str = datetime.now().strftime("%m/%d")
            cfg.days = today_str
            cfg.referer = update_referer_days(cfg.referer, today_str)

        log_info(
            f"配置加载: tel={cfg.tel} realname={cfg.realname} userno={cfg.userno} "
            f"cgid={cfg.cgid} ydxmid={cfg.ydxmid} cdid={cfg.cdid} days={cfg.days}"
        )
        s = build_session(cfg)

        # 子命令：只输出绿色可预约数量
        if len(sys.argv) >= 2 and sys.argv[1].lower() in {"list", "ls", "avail"}:
            log_info("模式=list：开始查询可预约格子")
            kyy = list_available_cells(
                s, cgid=cfg.cgid, ydxmid=cfg.ydxmid, days=cfg.days, log_request=False
            )
            avail = [c for c in kyy if c.get("ygq") != "1"]
            booked = list_booked_cells(
                s, cgid=cfg.cgid, ydxmid=cfg.ydxmid, days=cfg.days, log_request=False
            )
            booked_ids = {c["select_id"] for c in booked}
            avail_ids = {c["select_id"] for c in avail} - booked_ids  # 橙色优先覆盖
            log_info(f"绿色可预约数量={len(avail_ids)}")
            return 0

        # 0) 10 秒内：没抢到自动换格子继续抢（成功即停止）
        max_retry_seconds = 10.0
        deadline = time.perf_counter() + max_retry_seconds
        base_referer = cfg.referer

        tried: set = set()
        attempt_no = 0

        while time.perf_counter() < deadline:
            attempt_no += 1
            remaining_ms = int(max(0.0, (deadline - time.perf_counter())) * 1000)

            kyy = list_available_cells(s, cgid=cfg.cgid, ydxmid=cfg.ydxmid, days=cfg.days, log_request=False)
            avail = [c for c in kyy if c.get("ygq") != "1"]
            booked = list_booked_cells(s, cgid=cfg.cgid, ydxmid=cfg.ydxmid, days=cfg.days, log_request=False)
            booked_ids = {c["select_id"] for c in booked}
            green_ids = {c["select_id"] for c in avail} - booked_ids
            log_info(f"[重试 {attempt_no}] 当前绿色可预约格子数量={len(green_ids)}（剩余{remaining_ms}ms）")

            if not green_ids:
                time.sleep(0.2)
                continue

            best = choose_best_select_id(
                avail_items=avail,
                booked_ids=booked_ids,
                prefer_sd=[20, 18],
                exclude_ids=tried,
            )

            if not best:
                # 都试过了，清空已试集合再来一轮
                tried.clear()
                time.sleep(0.1)
                continue

            tried.add(best)

            # 终端提示正在抢哪个格子
            try:
                sd_s, court_s = best.split("_", 1)
                log_info(f"正在尝试抢：{int(court_s)}号场 {sd_to_timerange(int(sd_s))}（cdid={best}）")
            except Exception:
                log_info(f"正在尝试抢：cdid={best}")

            ok, st, rid = attempt_one_cell(s, cfg=cfg, base_referer=base_referer, select_id=best)
            if ok:
                log_info(f"预约成功，停止重试：id={rid}")
                return 0

            # 未成功：快速进入下一轮（留一点点间隔，避免过于激进）
            time.sleep(0.05)

        log_warn(f"10 秒内未抢到任何可预约格子，停止。已尝试格子数={len(tried)}")
        return 1
    finally:
        log_info(f"程序总耗时={((time.perf_counter()-t0)*1000):.1f}ms")


if __name__ == "__main__":
    raise SystemExit(main())

