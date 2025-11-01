// 认证相关函数
const API_BASE_URL = '/api';

// 保存token到localStorage
function saveToken(token) {
    localStorage.setItem('token', token);
}

// 获取token
function getToken() {
    return localStorage.getItem('token');
}

// 保存用户信息
function saveUser(user) {
    localStorage.setItem('user', JSON.stringify(user));
}

// 获取用户信息
function getUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
}

// 清除认证信息
function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
}

// 检查是否已登录
function isAuthenticated() {
    return !!getToken();
}

// 登出
function logout() {
    clearAuth();
    window.location.href = '/login.html';
}

// 发送API请求
async function apiRequest(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const config = {
        ...options,
        headers
    };
    
    try {
        console.log('发起API请求:', API_BASE_URL + url, config);
        const response = await fetch(API_BASE_URL + url, config);
        
        console.log('响应状态:', response.status, response.statusText);
        
        // 对于登录请求，不要因为401就跳转
        const isLoginRequest = url.includes('/auth/login');
        
        // 只在真正的认证错误时才跳转到登录页
        if (response.status === 401 && !isLoginRequest) {
            console.error('未授权错误（401）- 需要重新登录');
            console.error('请求URL:', API_BASE_URL + url);
            clearAuth();
            window.location.href = '/login.html';
            throw new Error('未授权');
        }
        
        // 403错误：权限不足，但不跳转到登录页
        if (response.status === 403) {
            console.error('权限不足（403）');
            console.error('请求URL:', API_BASE_URL + url);
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const responseText = await response.text();
                console.error('错误响应内容:', responseText);
                // 返回错误而不是跳转到登录页
                return JSON.parse(responseText);
            } else {
                throw new Error('权限不足');
            }
        }
        
        // 检查响应是否成功
        if (!response.ok) {
            console.error('HTTP错误 - 状态码:', response.status, response.statusText);
            throw new Error(`HTTP错误: ${response.status} ${response.statusText}`);
        }
        
        // 检查响应内容类型
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            console.error('响应不是JSON格式 - Content-Type:', contentType);
            throw new Error('响应不是JSON格式');
        }
        
        // 获取响应文本
        const responseText = await response.text();
        console.log('API响应文本:', responseText);
        
        // 检查响应是否为空
        if (!responseText.trim()) {
            console.error('响应为空');
            throw new Error('响应为空');
        }
        
        // 解析JSON
        const data = JSON.parse(responseText);
        return data;
    } catch (error) {
        console.error('API请求错误:', error);
        throw error;
    }
}

// 显示消息
function showMessage(message, type = 'success') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    
    const container = document.querySelector('.card') || document.body;
    container.insertBefore(alertDiv, container.firstChild);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

// 登录
async function login(username, password) {
    const data = await apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });
    
    console.log('登录响应数据:', data);
    
    if (data.code === 200) {
        console.log('登录成功，保存token:', data.data.token);
        console.log('保存用户信息:', data.data.user);
        saveToken(data.data.token);
        saveUser(data.data.user);
        return true;
    } else {
        console.error('登录失败:', data.message);
        throw new Error(data.message);
    }
}

// 注册
async function register(formData) {
    const data = await apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify(formData)
    });
    
    if (data.code === 200) {
        saveToken(data.data.token);
        saveUser(data.data.user);
        return true;
    } else {
        throw new Error(data.message);
    }
}

// 页面加载时检查认证状态
function checkAuth() {
    const publicPages = ['/login.html', '/register.html', '/index.html', '/'];
    const currentPage = window.location.pathname;
    
    console.log('检查认证状态，当前页面:', currentPage);
    
    // 对于受保护的页面，检查登录状态
    if (!publicPages.includes(currentPage)) {
        console.log('这是受保护页面，检查登录状态');
        
        const hasToken = isAuthenticated();
        console.log('是否有token:', hasToken);
        
        if (!hasToken) {
            console.log('没有token，跳转到登录页');
            // 未登录，直接跳转到登录页
            alert('请先登录');
            window.location.href = '/login.html';
            return;
        }
        
        console.log('有token，允许访问');
        // 有token就允许访问
    } else {
        console.log('这是公开页面，无需检查认证');
    }
}

// 验证token是否有效
async function verifyToken() {
    try {
        console.log('开始验证token...');
        const token = getToken();
        console.log('验证token，当前token:', token ? token.substring(0, 20) + '...' : 'null');
        
        if (!token) {
            console.log('没有token，跳转到登录页');
            clearAuth();
            window.location.href = '/login.html';
            return;
        }
        
        console.log('发送验证请求到 /api/users/me');
        // 尝试获取当前用户信息以验证token
        const response = await fetch('/api/users/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        console.log('验证token响应状态:', response.status);
        
        if (response.status === 401 || response.status === 403) {
            // Token无效，清除并跳转到登录页
            console.log('Token无效，跳转到登录页');
            clearAuth();
            window.location.href = '/login.html';
        } else if (response.status === 200) {
            console.log('Token验证成功');
        } else {
            console.log('Token验证出现其他状态:', response.status);
        }
    } catch (error) {
        console.error('验证token失败:', error);
        // 验证失败，清除并跳转到登录页
        clearAuth();
        window.location.href = '/login.html';
    }
}

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
});

