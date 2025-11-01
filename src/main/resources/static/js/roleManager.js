// 角色权限管理模块
class RoleManager {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    init() {
        // 获取当前用户信息
        this.currentUser = this.getUser();
        if (this.currentUser) {
            this.applyRoleBasedUI();
        }
    }

    getUser() {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    }

    // 检查用户角色
    isAdmin() {
        return this.currentUser && this.currentUser.role === 'ADMIN';
    }

    isCounselor() {
        return this.currentUser && this.currentUser.role === 'COUNSELOR';
    }

    isWriter() {
        return this.currentUser && this.currentUser.role === 'WRITER';
    }

    isStudent() {
        return this.currentUser && this.currentUser.role === 'STUDENT';
    }

    // 检查是否有权限访问某个功能
    hasPermission(feature) {
        switch (feature) {
            case 'user_management':
                return this.isAdmin();
            case 'student_management':
                return this.isAdmin() || this.isCounselor();
            case 'university_management':
                return this.isAdmin();
            case 'application_management':
                return this.isAdmin() || this.isWriter();
            case 'view_applications':
                return this.isAdmin() || this.isCounselor() || this.isWriter();
            case 'view_universities':
                return this.isAdmin() || this.isCounselor() || this.isWriter();
            default:
                return false;
        }
    }

    // 根据角色应用UI权限控制
    applyRoleBasedUI() {
        // 隐藏/显示导航菜单
        this.updateNavigationMenu();
        
        // 隐藏/显示页面功能
        this.updatePageFeatures();
        
        // 显示角色信息
        this.displayRoleInfo();
    }

    // 更新导航菜单
    updateNavigationMenu() {
        const navLinks = document.querySelectorAll('.nav-links a');
        
        navLinks.forEach(link => {
            const href = link.getAttribute('href');
            
            // 根据页面和权限决定是否显示
            switch (href) {
                case '/dashboard.html':
                    // 仪表盘所有角色都可以访问
                    link.style.display = 'block';
                    break;
                case '/students.html':
                    // 学生管理：管理员和咨询顾问
                    link.style.display = this.hasPermission('student_management') ? 'block' : 'none';
                    break;
                case '/universities.html':
                    // 大学信息：所有角色都可以访问
                    link.style.display = 'block';
                    break;
                case '/applications.html':
                    // 申请管理：只有管理员和文案可以访问
                    link.style.display = (this.isAdmin() || this.isWriter()) ? 'block' : 'none';
                    break;
                case '/register.html':
                    // 注册页面：只有管理员可以访问
                    link.style.display = this.isAdmin() ? 'block' : 'none';
                    break;
                default:
                    // 退出登录按钮对所有角色都显示
                    if (href === '#' && link.textContent.includes('退出')) {
                        link.style.display = 'block';
                    } else {
                        link.style.display = 'block';
                    }
            }
        });
    }

    // 更新页面功能
    updatePageFeatures() {
        const currentPage = window.location.pathname;
        
        switch (currentPage) {
            case '/students.html':
                this.updateStudentPage();
                break;
            case '/universities.html':
                this.updateUniversityPage();
                break;
            case '/applications.html':
                this.updateApplicationPage();
                break;
        }
    }

    // 更新学生管理页面
    updateStudentPage() {
        // 隐藏添加学生按钮（只有管理员和咨询顾问可以添加学生）
        const addButton = document.querySelector('button[onclick="showAddModal()"]');
        if (addButton) {
            addButton.style.display = this.hasPermission('student_management') ? 'block' : 'none';
        }

        // 不再需要单独为顾问加载数据，因为students.html的loadStudents已经会调用
        // 后端API会根据用户角色自动过滤数据
    }

    // 更新大学信息页面
    updateUniversityPage() {
        // 所有角色都可以查看大学信息，但只有管理员可以添加大学
        const addButton = document.querySelector('button[onclick="showAddModal()"]');
        if (addButton) {
            addButton.style.display = this.hasPermission('university_management') ? 'block' : 'none';
        }
    }

    // 更新申请管理页面
    updateApplicationPage() {
        // 只有管理员和文案可以看到创建申请按钮
        const addButton = document.querySelector('button[onclick="showAddModal()"]');
        if (addButton) {
            addButton.style.display = this.hasPermission('application_management') ? 'block' : 'none';
        }

        // 不再需要单独为文案加载数据，因为applications.html的loadApplications已经会调用
        // 后端API会根据用户角色自动过滤数据
    }

    // 过滤咨询顾问的学生数据（只能看到自己负责的学生）
    filterCounselorStudentData() {
        if (this.isCounselor()) {
            // 只加载当前咨询顾问负责的学生数据
            loadCounselorStudentData();
        }
    }

    // 过滤文案的申请数据（只能看到自己负责的申请）
    filterWriterApplicationData() {
        if (this.isWriter()) {
            // 只加载当前文案负责的申请数据
            loadWriterApplicationData();
        }
    }

    // 显示角色信息
    displayRoleInfo() {
        const roleNames = {
            'ADMIN': '管理员',
            'COUNSELOR': '咨询顾问',
            'WRITER': '文案'
        };

        const roleName = roleNames[this.currentUser.role] || this.currentUser.role;
        
        // 在用户名旁边显示角色
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.innerHTML = `${this.currentUser.realName || this.currentUser.username} <small style="color: #666;">(${roleName})</small>`;
        }

        // 在页面标题显示角色
        const pageTitle = document.querySelector('h1');
        if (pageTitle && this.isWriter()) {
            pageTitle.innerHTML = pageTitle.innerHTML.replace('管理', '查看');
        }
    }

    // 显示权限提示
    showPermissionDenied(message = '您没有权限执行此操作') {
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-error';
        alertDiv.innerHTML = `
            <strong>权限不足</strong><br>
            ${message}<br>
            <small>请联系管理员获取相应权限</small>
        `;
        
        const container = document.querySelector('.card') || document.body;
        container.insertBefore(alertDiv, container.firstChild);
        
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }

    // 检查操作权限
    checkOperationPermission(operation, showAlert = true) {
        const hasPermission = this.hasPermission(operation);
        
        if (!hasPermission && showAlert) {
            this.showPermissionDenied();
        }
        
        return hasPermission;
    }
}

// 全局角色管理器实例
let roleManager;

// 页面加载时初始化角色管理
document.addEventListener('DOMContentLoaded', () => {
    roleManager = new RoleManager();
});

// 咨询顾问角色专用函数
async function loadCounselorStudentData() {
    try {
        // 获取当前咨询顾问负责的学生数据 - 使用正确的分页API
        // 后端已经根据角色过滤了数据
        const response = await apiRequest('/students?page=0&size=10');
        if (response.code === 200) {
            // 后端已经根据角色过滤了数据
            if (typeof displayStudents === 'function') {
                if (response.data.content) {
                    displayStudents(response.data.content);
                } else {
                    displayStudents(response.data);
                }
            } else {
                console.error('displayStudents函数不存在');
            }
        } else {
            if (document.getElementById('studentsTable')) {
                document.getElementById('studentsTable').innerHTML = `
                    <div style="text-align: center; padding: 40px;">
                        <h3>暂无负责的学生</h3>
                        <p>您还没有被分配负责任何学生。</p>
                    </div>
                `;
            }
        }
    } catch (error) {
        console.error('加载咨询顾问学生数据失败:', error);
        if (roleManager && typeof roleManager.showPermissionDenied === 'function') {
            roleManager.showPermissionDenied('无法加载学生信息');
        }
    }
}

// 文案角色专用函数
async function loadWriterApplicationData() {
    try {
        // 获取当前文案负责的申请数据 - 使用正确的分页API
        const response = await apiRequest('/applications?page=0&size=10');
        if (response.code === 200) {
            // 后端已经根据角色过滤了数据
            if (typeof displayApplications === 'function') {
                if (response.data.content) {
                    displayApplications(response.data.content);
                } else {
                    displayApplications(response.data);
                }
            } else {
                console.error('displayApplications函数不存在');
            }
        } else {
            if (document.getElementById('applicationsTable')) {
                document.getElementById('applicationsTable').innerHTML = `
                    <div style="text-align: center; padding: 40px;">
                        <h3>暂无负责的申请</h3>
                        <p>您还没有被分配负责任何申请。</p>
                    </div>
                `;
            }
        }
    } catch (error) {
        console.error('加载文案申请数据失败:', error);
        if (roleManager && typeof roleManager.showPermissionDenied === 'function') {
            roleManager.showPermissionDenied('无法加载申请信息');
        }
    }
}

// 学生角色专用函数（保留用于兼容性）
async function loadCurrentStudentData() {
    try {
        // 获取当前用户的学生信息
        const response = await apiRequest('/users/me');
        if (response.code === 200) {
            const user = response.data;
            const studentResponse = await apiRequest(`/students/user/${user.id}`);
            if (studentResponse.code === 200 && studentResponse.data) {
                // 只显示当前学生的信息
                displayStudents([studentResponse.data]);
            } else {
                // 学生信息不存在，显示提示
                document.getElementById('studentsTable').innerHTML = `
                    <div style="text-align: center; padding: 40px;">
                        <h3>欢迎使用系统！</h3>
                        <p>您还没有完善个人信息，请先填写学生资料。</p>
                        <button class="btn" onclick="showAddStudentForCurrentUser()">完善我的资料</button>
                    </div>
                `;
            }
        }
    } catch (error) {
        console.error('加载学生数据失败:', error);
        roleManager.showPermissionDenied('无法加载学生信息');
    }
}

// 为当前用户添加学生信息
async function showAddStudentForCurrentUser() {
    try {
        const response = await apiRequest('/users/me');
        if (response.code === 200) {
            const user = response.data;
            document.getElementById('userId').value = user.id;
            
            // 预填充用户信息
            document.getElementById('username').value = user.username;
            document.getElementById('realName').value = user.realName;
            document.getElementById('email').value = user.email;
            document.getElementById('phone').value = user.phone;
            
            // 隐藏用户信息字段（因为用户已存在）
            document.querySelectorAll('input[name="username"], input[name="email"], input[name="phone"]').forEach(field => {
                field.parentElement.style.display = 'none';
            });
            document.querySelector('hr').style.display = 'none';
            document.querySelector('h4').style.display = 'none';
            
            document.getElementById('modalTitle').textContent = '完善我的资料';
            document.getElementById('studentModal').style.display = 'block';
        }
    } catch (error) {
        console.error('获取用户信息失败:', error);
        showMessage('获取用户信息失败', 'error');
    }
}

async function loadCurrentStudentApplications() {
    try {
        // 获取当前用户的学生信息
        const response = await apiRequest('/users/me');
        if (response.code === 200) {
            const user = response.data;
            const studentResponse = await apiRequest(`/students/user/${user.id}`);
            if (studentResponse.code === 200 && studentResponse.data) {
                const studentId = studentResponse.data.id;
                const applicationsResponse = await apiRequest(`/applications?studentId=${studentId}`);
                if (applicationsResponse.code === 200) {
                    displayApplications(applicationsResponse.data);
                }
            } else {
                document.getElementById('applicationsTable').innerHTML = `
                    <div style="text-align: center; padding: 40px;">
                        <h3>还没有申请记录</h3>
                        <p>您可以开始创建您的留学申请。</p>
                        <button class="btn" onclick="showAddModal()">创建申请</button>
                    </div>
                `;
            }
        }
    } catch (error) {
        console.error('加载申请数据失败:', error);
        roleManager.showPermissionDenied('无法加载申请信息');
    }
}
