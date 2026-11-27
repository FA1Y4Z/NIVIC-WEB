const API_URL = (() => {
    if (window.NIVIC_API_URL) return window.NIVIC_API_URL;

    const host = location.hostname;
    const isLocal =
        host === 'localhost' ||
        host === '127.0.0.1' ||
        /^192\.168\./.test(host) ||
        /^10\./.test(host);

    return isLocal
        ? `http://${host}:8080`
        : 'https://nivic.onrender.com';
})();

const TOKEN_KEY = 'nivic_token';

const $ = (selector) => document.querySelector(selector);
const all = (selector) => [...document.querySelectorAll(selector)];

const brl = (value) =>
    Number(value || 0).toLocaleString('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    });

const dateNow = () => {
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
};

const esc = (value) =>
    String(value ?? '').replace(/[&<>"']/g, (character) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[character]);

const normalize = (value) =>
    String(value ?? '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase();

const byteLength = (value) => new TextEncoder().encode(value).length;

let token = sessionStorage.getItem(TOKEN_KEY);
let me = null;
let page = 'dashboard';
let editing = null;
let searchTerm = '';
let lastFocusedElement = null;

let store = {
    receitas: [],
    despesas: [],
    metas: []
};
let dashboardCharts = {
    monthly: null,
    categories: null
};

async function api(path, options = {}) {
    const headers = {
        Accept: 'application/json',
        ...(options.body ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers
    };

    let response;

    try {
        response = await fetch(`${API_URL}${path}`, {
            ...options,
            headers
        });
    } catch (error) {
        throw new Error(
            'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.'
        );
    }

    const responseText = await response.text();
    let result = null;

    if (responseText) {
        try {
            result = JSON.parse(responseText);
        } catch {
            result = responseText;
        }
    }

    if (!response.ok) {
        if (response.status === 401 && token) {
            clearSession();
            auth('login');
            throw new Error('Sessão expirada. Faça login novamente.');
        }

        let message = '';

        if (typeof result === 'string' && result.trim()) {
            message = result;
        } else if (result?.erro) {
            message = result.erro;
        } else if (result?.mensagem) {
            message = result.mensagem;
        } else if (result?.message) {
            message = result.message;
        } else if (result?.detail) {
            message = result.detail;
        }

        if (!message) {
            if (response.status === 400) {
                message = 'Dados inválidos. Confira os campos preenchidos.';
            } else if (response.status === 404) {
                message = 'Registro ou rota não encontrado.';
            } else if (response.status === 409) {
                message = 'Este cadastro já existe.';
            } else {
                message = 'Erro na comunicação com o servidor.';
            }
        }

        throw new Error(message);
    }

    return result;
}

function clearSession() {
    token = null;
    me = null;
    searchTerm = '';

    sessionStorage.removeItem(TOKEN_KEY);

    store = {
        receitas: [],
        despesas: [],
        metas: []
    };
}

function showLanding() {
    $('#landing').classList.remove('hide');
    $('#auth').classList.add('hide');
    $('#application').classList.add('hide');
    $('#modal').classList.add('hide');
    window.scrollTo(0, 0);
}

function auth(mode) {
    const login = mode === 'login';

    $('#landing').classList.add('hide');
    $('#auth').classList.remove('hide');
    $('#application').classList.add('hide');
    $('#modal').classList.add('hide');

    $('#loginForm').classList.toggle('hide', !login);
    $('#registerForm').classList.toggle('hide', login);

    $('#loginError').textContent = '';
    $('#regError').textContent = '';

    $('#authText').innerHTML = login
        ? 'Seu controle financeiro,<br>mais simples.'
        : 'Crie sua conta e organize<br>suas finanças.';

    $('#switch').innerHTML = login
        ? 'Não tem uma conta? <button type="button" data-auth="register">Cadastre-se</button>'
        : 'Já tem uma conta? <button type="button" data-auth="login">Faça login</button>';

    window.scrollTo(0, 0);
}

function enter(user) {
    me = user;

    const name = String(me?.nome || '').trim() || 'Usuário';

    $('#landing').classList.add('hide');
    $('#auth').classList.add('hide');
    $('#application').classList.remove('hide');

    $('#userName').textContent = name;
    $('#avatar').textContent = name[0].toUpperCase();

    go('dashboard');
}

$('#switch').addEventListener('click', (event) => {
    const button = event.target.closest('button[data-auth]');

    if (button) auth(button.dataset.auth);
});

$('#openLogin').addEventListener('click', () => auth('login'));
$('#openRegister').addEventListener('click', () => auth('register'));
$('#heroRegister').addEventListener('click', () => auth('register'));
$('#bottomRegister').addEventListener('click', () => auth('register'));
$('#backToSite').addEventListener('click', showLanding);

$('#authBrand').addEventListener('click', (event) => {
    event.preventDefault();
    showLanding();
});

const currentYear = new Date().getFullYear();
$('#year').textContent = currentYear;
$('#appYear').textContent = currentYear;

async function loadUserAndData() {
    const [user, receitas, despesas, metas] = await Promise.all([
        api('/api/usuarios/me'),
        api('/api/receitas'),
        api('/api/despesas'),
        api('/api/metas')
    ]);

    me = user;
    store.receitas = receitas;
    store.despesas = despesas;
    store.metas = metas;
}

async function refreshData() {
    const [receitas, despesas, metas] = await Promise.all([
        api('/api/receitas'),
        api('/api/despesas'),
        api('/api/metas')
    ]);

    store.receitas = receitas;
    store.despesas = despesas;
    store.metas = metas;
}

$('#loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    $('#loginError').textContent = '';

    const button = $('#loginForm button[type="submit"]');
    button.disabled = true;

    try {
        const result = await api('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({
                email: $('#loginEmail').value.trim().toLowerCase(),
                senha: $('#loginPass').value
            })
        });

        token = result.token;
        sessionStorage.setItem(TOKEN_KEY, token);

        await loadUserAndData();
        enter(me);

        $('#loginForm').reset();
        notify('Login realizado com sucesso!');
    } catch (error) {
        $('#loginError').textContent = error.message;
    } finally {
        button.disabled = false;
    }
});

$('#registerForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    $('#regError').textContent = '';

    const nome = $('#regName').value.trim();
    const email = $('#regEmail').value.trim().toLowerCase();
    const senha = $('#regPass').value;
    const confirmacao = $('#regConfirm').value;

    if (!nome) {
        $('#regError').textContent = 'Informe seu nome completo.';
        return;
    }

    if (senha !== confirmacao) {
        $('#regError').textContent = 'As senhas não coincidem.';
        return;
    }

    if (senha.length < 8) {
        $('#regError').textContent = 'A senha precisa ter pelo menos 8 caracteres.';
        return;
    }

    if (byteLength(senha) > 72) {
        $('#regError').textContent =
            'A senha é muito longa (máximo de 72 bytes; acentos e emojis contam mais).';
        return;
    }

    if (!$('#acceptTerms').checked) {
        $('#regError').textContent =
            'Você precisa aceitar os Termos de Uso e a Política de Privacidade.';
        return;
    }

    const button = $('#registerForm button[type="submit"]');
    button.disabled = true;

    try {
        await api('/api/usuarios', {
            method: 'POST',
            body: JSON.stringify({ nome, email, senha })
        });

        const result = await api('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, senha })
        });

        token = result.token;
        sessionStorage.setItem(TOKEN_KEY, token);

        await loadUserAndData();
        enter(me);

        $('#registerForm').reset();
        notify('Conta criada com sucesso!');
    } catch (error) {
        $('#regError').textContent = error.message;
    } finally {
        button.disabled = false;
    }
});

function logout() {
    clearSession();
    auth('login');

    $('#loginForm').reset();
    $('#registerForm').reset();

    notify('Você saiu da sua conta.');
}

$('#logout').addEventListener('click', logout);

all('[data-page]').forEach(button => {
    button.addEventListener('click', () => {
        go(button.dataset.page);
    });
});

function go(p) {
    if (p !== page) searchTerm = '';

    page = p;

    const names = {
        dashboard: 'Dashboard financeiro',
        receitas: 'Receitas',
        despesas: 'Despesas',
        metas: 'Metas financeiras',
        conta: 'Minha conta'
    };

    $('#title').textContent = names[p];

    all('[data-page]').forEach(button => {
        button.classList.toggle('selected', button.dataset.page === p);
    });

    render();
}

function totals() {
    const inc = store.receitas.reduce(
        (sum, item) => sum + Number(item.valor || 0),
        0
    );

    const out = store.despesas.reduce(
        (sum, item) => sum + Number(item.valor || 0),
        0
    );

    return {
        inc,
        out,
        balance: inc - out
    };
}

function fmt(date) {
    if (!date) return '—';

    const parts = String(date).slice(0, 10).split('-');

    if (parts.length !== 3) return date;

    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

function pct(goal) {
    const target = Number(goal.valorObjetivo ?? goal.valor_objetivo ?? 0);
    const current = Number(goal.valorAtual ?? goal.valor_atual ?? 0);

    if (target <= 0) return 0;

    return Math.max(0, Math.min(100, Math.round((current / target) * 100)));
}

function destroyDashboardCharts() {
    if (dashboardCharts.monthly) {
        dashboardCharts.monthly.destroy();
        dashboardCharts.monthly = null;
    }

    if (dashboardCharts.categories) {
        dashboardCharts.categories.destroy();
        dashboardCharts.categories = null;
    }
}

function getMonthlyData() {
    const months = [];

    for (let i = 5; i >= 0; i--) {
        const date = new Date();

        date.setDate(1);
        date.setMonth(date.getMonth() - i);

        months.push({
            key: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`,
            label: date.toLocaleDateString('pt-BR', {
                month: 'short'
            })
        });
    }

    const income = months.map(month =>
        store.receitas
            .filter(item => String(item.data).slice(0, 7) === month.key)
            .reduce((sum, item) => sum + Number(item.valor || 0), 0)
    );

    const expenses = months.map(month =>
        store.despesas
            .filter(item => String(item.data).slice(0, 7) === month.key)
            .reduce((sum, item) => sum + Number(item.valor || 0), 0)
    );

    return {
        labels: months.map(month => month.label),
        income,
        expenses
    };
}

function getCategoryData() {
    const categories = {};

    store.despesas.forEach(item => {
        const category = item.categoria || 'Sem categoria';

        categories[category] =
            (categories[category] || 0) + Number(item.valor || 0);
    });

    return {
        labels: Object.keys(categories),
        values: Object.values(categories)
    };
}

function renderDashboardCharts() {
    const monthlyCanvas = document.getElementById('monthlyChart');
    const categoryCanvas = document.getElementById('categoryChart');
    const categoryEmpty = document.getElementById('categoryEmpty');

    if (!monthlyCanvas || !categoryCanvas || typeof Chart === 'undefined') {
        return;
    }

    const monthly = getMonthlyData();

    dashboardCharts.monthly = new Chart(monthlyCanvas, {
        type: 'bar',

        data: {
            labels: monthly.labels,

            datasets: [
                {
                    label: 'Receitas',
                    data: monthly.income,
                    backgroundColor: '#16c99e',
                    borderRadius: 7
                },
                {
                    label: 'Despesas',
                    data: monthly.expenses,
                    backgroundColor: '#df5967',
                    borderRadius: 7
                }
            ]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,

            plugins: {
                legend: {
                    position: 'bottom'
                },

                tooltip: {
                    callbacks: {
                        label: context =>
                            `${context.dataset.label}: ${brl(context.raw)}`
                    }
                }
            },

            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: value => brl(value)
                    }
                }
            }
        }
    });

    const categories = getCategoryData();

    if (!categories.labels.length) {
        categoryCanvas.parentElement.classList.add('hide');
        categoryEmpty.classList.remove('hide');
        return;
    }

    categoryCanvas.parentElement.classList.remove('hide');
    categoryEmpty.classList.add('hide');

    dashboardCharts.categories = new Chart(categoryCanvas, {
        type: 'doughnut',

        data: {
            labels: categories.labels,

            datasets: [{
                data: categories.values,

                backgroundColor: [
                    '#079b7c',
                    '#16c99e',
                    '#f2b45b',
                    '#df5967',
                    '#6b8fd6',
                    '#9674c8',
                    '#53a6b5'
                ],

                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,

            plugins: {
                legend: {
                    position: 'bottom'
                },

                tooltip: {
                    callbacks: {
                        label: context =>
                            `${context.label}: ${brl(context.raw)}`
                    }
                }
            }
        }
    });
}


function render() {
    if (!me) return;

    const pages = {
        dashboard,
        receitas: () => list('receitas'),
        despesas: () => list('despesas'),
        metas: goals,
        conta: account
    };

    destroyDashboardCharts();

    $('#content').innerHTML = pages[page]();

    if (page === 'dashboard') {
        renderDashboardCharts();
    }
} 

function applySearch() {
    const query = normalize(searchTerm);

    all('#tbody tr[data-q]').forEach(row => {
        row.classList.toggle('hide', !row.dataset.q.includes(query));
    });
}

function dashboard() {
    const t = totals();
    const firstName = esc(String(me.nome || 'Usuário').trim().split(' ')[0]);

    const movements = [
        ...store.receitas.map(item => ({ ...item, kind: 'Receita' })),
        ...store.despesas.map(item => ({ ...item, kind: 'Despesa' }))
    ]
        .sort((a, b) => String(b.data).localeCompare(String(a.data)))
        .slice(0, 5);

    return `
        <div class="welcome">
            <h2>Olá, ${firstName}!</h2>
            <p>Seja bem-vindo(a) ao NIVIC. Aqui está um resumo da sua vida financeira.</p>
        </div>

        <div class="stats">
            <article class="card">
                <div class="stat-label">Saldo atual ◈</div>
                <div class="stat-value">${brl(t.balance)}</div>
                <div class="muted">Receitas menos despesas</div>
            </article>

            <article class="card">
                <div class="stat-label">↗ Total de receitas</div>
                <div class="stat-value green">${brl(t.inc)}</div>
                <div class="muted">Valores registrados</div>
            </article>

            <article class="card">
                <div class="stat-label">↘ Total de despesas</div>
                <div class="stat-value red">${brl(t.out)}</div>
                <div class="muted">Valores registrados</div>
            </article>
        </div>

<div class="charts-grid">

    <section class="chart-panel">
        <div class="chart-header">
            <h3>Receitas x Despesas</h3>
            <p>Comparativo dos últimos seis meses</p>
        </div>

        <div class="chart-container">
            <canvas id="monthlyChart"></canvas>
        </div>
    </section>

    <section class="chart-panel">
        <div class="chart-header">
            <h3>Despesas por categoria</h3>
            <p>Distribuição dos seus gastos</p>
        </div>

        <div class="chart-container">
            <canvas id="categoryChart"></canvas>
        </div>

        <div id="categoryEmpty" class="chart-empty hide">
            Nenhuma despesa cadastrada ainda.
        </div>
    </section>

</div>

        <div class="dash-grid">
            <section class="panel">
                <h3>Movimentações recentes</h3>

                ${movements.length
                    ? movements.map(item => `
                        <div class="movement">
                            <span>
                                ${esc(item.descricao)}
                                <small>${fmt(item.data)} · ${item.kind}</small>
                            </span>

                            <b class="${item.kind === 'Receita' ? 'green' : 'red'}">
                                ${item.kind === 'Receita' ? '+' : '−'}
                                ${brl(item.valor)}
                            </b>
                        </div>
                    `).join('')
                    : '<div class="empty">Nenhuma movimentação cadastrada ainda.</div>'
                }
            </section>

            <section class="panel">
                <h3>Resumo das metas</h3>

                ${store.metas.length
                    ? store.metas.slice(0, 4).map(goal => `
                        <div class="goal-mini">
                            <span>${esc(goal.nome)}</span>
                            <b>${pct(goal)}%</b>
                        </div>

                        <div class="progress" style="margin-bottom:12px">
                            <span style="width:${pct(goal)}%"></span>
                        </div>
                    `).join('')
                    : '<div class="empty">Você ainda não possui metas.</div>'
                }
            </section>
        </div>
    `;
}

function list(type) {
    const income = type === 'receitas';
    const items = store[type];

    return `
        <div class="page-head">
            <div>
                <h2>${income ? 'Receitas' : 'Despesas'}</h2>
                <p>Gerencie seus lançamentos financeiros.</p>
            </div>

            <button class="primary small" id="add">
                ＋ Nova ${income ? 'receita' : 'despesa'}
            </button>
        </div>

        <div class="toolbar">
            <label class="sr-only" for="search">Buscar lançamentos</label>
            <input
                id="search"
                class="search"
                placeholder="⌕ Buscar por descrição ou ID..."
                autocomplete="off"
            >
        </div>

        <div class="table-wrap">
            <table class="data">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Descrição</th>
                        <th>${income ? 'Tipo' : 'Categoria'}</th>
                        <th>Valor</th>
                        <th>Data</th>
                        <th>Ações</th>
                    </tr>
                </thead>

                <tbody id="tbody">
                    ${items.length
                        ? items.map(item => {
                            const detail = income ? item.tipo : item.categoria;

                            return `
                                <tr data-q="${esc(
                                    normalize(`${item.id} ${item.descricao} ${detail || ''}`)
                                )}">
                                    <td>#${item.id}</td>
                                    <td>${esc(item.descricao)}</td>
                                    <td>${esc(detail || '—')}</td>
                                    <td><b>${brl(item.valor)}</b></td>
                                    <td>${fmt(item.data)}</td>
                                    <td>
                                        <button class="action" data-edit="${item.id}" data-type="${type}">
                                            Editar
                                        </button>

                                        <button class="action delete" data-del="${item.id}" data-type="${type}">
                                            Excluir
                                        </button>
                                    </td>
                                </tr>
                            `;
                        }).join('')
                        : '<tr><td colspan="6" class="empty">Nenhum registro cadastrado.</td></tr>'
                    }
                </tbody>
            </table>
        </div>
    `;
}

function goals() {
    return `
        <div class="page-head">
            <div>
                <h2>Metas financeiras</h2>
                <p>Acompanhe seus objetivos e veja sua evolução.</p>
            </div>

            <button class="primary small" id="addGoal">
                ＋ Nova meta
            </button>
        </div>

        ${store.metas.length
            ? `
                <div class="goal-grid">
                    ${store.metas.map(goal => `
                        <article class="goal">
                            <div class="goal-top">
                                <h3>${esc(goal.nome)}</h3>
                                <span class="goal-percent">${pct(goal)}%</span>
                            </div>

                            <div class="goal-values">
                                <span>
                                    Acumulado:
                                    <b>${brl(goal.valorAtual ?? goal.valor_atual)}</b>
                                </span>

                                <span>
                                    Meta:
                                    <b>${brl(goal.valorObjetivo ?? goal.valor_objetivo)}</b>
                                </span>
                            </div>

                            <div class="progress">
                                <span style="width:${pct(goal)}%"></span>
                            </div>

                            <div class="goal-foot">
                                <span>
                                    ${goal.dataLimite || goal.data_limite
                                        ? 'Prazo: ' + fmt(goal.dataLimite || goal.data_limite)
                                        : 'Sem prazo definido'}
                                </span>

                                <span>
                                    <button class="action" data-edit="${goal.id}" data-type="metas">
                                        Editar
                                    </button>

                                    <button class="action delete" data-del="${goal.id}" data-type="metas">
                                        Excluir
                                    </button>
                                </span>
                            </div>
                        </article>
                    `).join('')}
                </div>
            `
            : '<div class="panel empty">Você ainda não cadastrou metas financeiras.</div>'
        }
    `;
}

function account() {
    return `
        <div class="page-head">
            <div>
                <h2>Minha conta</h2>
                <p>Informações do usuário autenticado.</p>
            </div>
        </div>

        <div class="profile-grid">
            <section class="profile">
                <h3>Dados pessoais</h3>

                <div class="field">
                    <label for="accName">Nome completo</label>
                    <input id="accName" value="${esc(me.nome)}" disabled>
                </div>

                <div class="field">
                    <label for="accEmail">E-mail</label>
                    <input id="accEmail" value="${esc(me.email)}" disabled>
                </div>

                <p class="muted">
                    A edição dos dados pessoais ainda não está disponível.
                </p>
            </section>

            <section class="profile">
                <h3>Segurança</h3>

                <p class="muted">
                    Sua conta utiliza autenticação JWT.
                    O token é removido ao encerrar a sessão.
                </p>

                <button class="secondary" id="accountLogout" style="margin-top:16px">
                    Encerrar sessão
                </button>
            </section>
        </div>
    `;
}

function openModal(type, item = null) {
    editing = {
        type,
        id: item ? Number(item.id) : null
    };

    lastFocusedElement = document.activeElement;

    const goal = type === 'metas';
    const income = type === 'receitas';

    $('#modalTitle').textContent =
        (item ? 'Editar ' : 'Nova ') +
        (goal ? 'meta' : income ? 'receita' : 'despesa');

    let fields = '';

    if (goal) {
        fields = `
            <div class="field">
                <label for="f-nome">Nome da meta</label>
                <input
                    id="f-nome"
                    name="nome"
                    required
                    value="${esc(item?.nome || '')}"
                    placeholder="Ex.: Viagem dos sonhos"
                >
            </div>

            <div class="field">
                <label for="f-valor">Valor desejado (R$)</label>
                <input
                    id="f-valor"
                    name="valor"
                    type="number"
                    min="0.01"
                    step="0.01"
                    required
                    value="${item?.valorObjetivo ?? item?.valor_objetivo ?? ''}"
                >
            </div>

            <div class="field">
                <label for="f-acumulado">Valor acumulado (R$)</label>
                <input
                    id="f-acumulado"
                    name="acumulado"
                    type="number"
                    min="0"
                    step="0.01"
                    required
                    value="${item?.valorAtual ?? item?.valor_atual ?? 0}"
                >
            </div>

            <div class="field">
                <label for="f-prazo">Prazo</label>
                <input
                    id="f-prazo"
                    name="prazo"
                    type="date"
                    required
                    value="${String(item?.dataLimite || item?.data_limite || '').slice(0, 10)}"
                >
            </div>
        `;
    } else {
        fields = `
            <div class="field">
                <label for="f-descricao">Descrição</label>
                <input
                    id="f-descricao"
                    name="descricao"
                    required
                    value="${esc(item?.descricao || '')}"
                    placeholder="Descrição do lançamento"
                >
            </div>

            <div class="field">
                <label for="f-valor">Valor (R$)</label>
                <input
                    id="f-valor"
                    name="valor"
                    type="number"
                    min="0.01"
                    step="0.01"
                    required
                    value="${item?.valor ?? ''}"
                >
            </div>

            <div class="field">
                <label for="f-detalhe">${income ? 'Tipo da receita' : 'Categoria da despesa'}</label>
                <input
                    id="f-detalhe"
                    name="${income ? 'tipo' : 'categoria'}"
                    required
                    value="${esc(income ? item?.tipo || '' : item?.categoria || '')}"
                    placeholder="${income ? 'Ex.: Salário, freelance...' : 'Ex.: Alimentação, transporte...'}"
                >
            </div>

            <div class="field">
                <label for="f-data">Data</label>
                <input
                    id="f-data"
                    name="data"
                    type="date"
                    required
                    value="${String(item?.data || dateNow()).slice(0, 10)}"
                >
            </div>
        `;
    }

    $('#modalFields').innerHTML = fields;
    $('#modalError').textContent = '';
    $('#modal').classList.remove('hide');

    $('#modalFields input')?.focus();
}

function closeModal() {
    $('#modal').classList.add('hide');
    $('#recordForm').reset();
    editing = null;

    if (lastFocusedElement && document.contains(lastFocusedElement)) {
        lastFocusedElement.focus();
    }

    lastFocusedElement = null;
}

$('#close').addEventListener('click', closeModal);
$('#cancel').addEventListener('click', closeModal);

$('#modal').addEventListener('mousedown', (event) => {
    if (event.target === $('#modal')) closeModal();
});

document.addEventListener('keydown', (event) => {
    if ($('#modal').classList.contains('hide')) return;

    if ($('#nivic-confirm-overlay')) return;

    if (event.key === 'Escape') {
        closeModal();
        return;
    }

    if (event.key === 'Tab') {
        const focusable = all(
            '#recordForm input:not([disabled]), #recordForm button:not([disabled])'
        );

        if (!focusable.length) return;

        const first = focusable[0];
        const last = focusable[focusable.length - 1];

        if (event.shiftKey && document.activeElement === first) {
            event.preventDefault();
            last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    }
});

$('#recordForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    if (!editing) return;

    const form = Object.fromEntries(new FormData(event.target).entries());
    const { type, id } = editing;

    let endpoint = '';
    let body = {};

    if (type === 'receitas') {
        endpoint = '/api/receitas';

        body = {
            descricao: form.descricao.trim(),
            valor: Number(form.valor),
            tipo: form.tipo.trim(),
            data: form.data
        };
    } else if (type === 'despesas') {
        endpoint = '/api/despesas';

        body = {
            descricao: form.descricao.trim(),
            valor: Number(form.valor),
            categoria: form.categoria.trim(),
            data: form.data
        };
    } else if (type === 'metas') {
        endpoint = '/api/metas';

        body = {
            nome: form.nome.trim(),
            valorObjetivo: Number(form.valor),
            valorAtual: Number(form.acumulado),
            dataLimite: form.prazo
        };
    }

    if (type === 'metas') {
        if (
            !body.nome ||
            !Number.isFinite(body.valorObjetivo) ||
            !Number.isFinite(body.valorAtual) ||
            body.valorObjetivo <= 0 ||
            body.valorAtual < 0 ||
            !body.dataLimite
        ) {
            $('#modalError').textContent =
                'Confira os valores e informe um prazo para a meta.';
            return;
        }
    } else {
        if (
            !body.descricao ||
            !Number.isFinite(body.valor) ||
            body.valor <= 0 ||
            !body.data
        ) {
            $('#modalError').textContent =
                'Preencha todos os campos corretamente.';
            return;
        }
    }

    const button = $('#recordForm button.primary');
    button.disabled = true;

    try {
        await api(id ? `${endpoint}/${id}` : endpoint, {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(body)
        });

        await refreshData();
        closeModal();
        render();

        notify(id ? 'Registro atualizado!' : 'Registro cadastrado!');
    } catch (error) {
        $('#modalError').textContent = error.message;
    } finally {
        button.disabled = false;
    }
});

$('#content').addEventListener('click', async (event) => {
    const button = event.target.closest('button');

    if (!button) return;

    if (button.id === 'add') {
        openModal(page);
        return;
    }

    if (button.id === 'addGoal') {
        openModal('metas');
        return;
    }

    if (button.id === 'accountLogout') {
        logout();
        return;
    }

    if (button.dataset.edit) {
        const type = button.dataset.type;

        const item = store[type].find(
            entry => Number(entry.id) === Number(button.dataset.edit)
        );

        if (item) openModal(type, item);

        return;
    }

    if (button.dataset.del) {
        const confirmed = await confirmAction(
            'Excluir registro',
            'Tem certeza de que deseja excluir este registro? Essa ação não poderá ser desfeita.'
        );

        if (!confirmed) return;

        const type = button.dataset.type;
        const id = Number(button.dataset.del);

        try {
            await api(`/api/${type}/${id}`, {
                method: 'DELETE'
            });

            await refreshData();
            render();

            notify('Registro excluído.');
        } catch (error) {
            notify(error.message);
        }
    }
});

$('#content').addEventListener('input', (event) => {
    if (event.target.id !== 'search') return;

    searchTerm = event.target.value;
    applySearch();
});

function confirmAction(title, message) {
    return new Promise((resolve) => {
        const existing = $('#nivic-confirm-overlay');

        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.id = 'nivic-confirm-overlay';

        overlay.innerHTML = `
            <div class="nivic-confirm-card" role="alertdialog" aria-modal="true" aria-labelledby="nivic-confirm-title">
                <div class="nivic-confirm-icon">!</div>
                <h3 id="nivic-confirm-title">${esc(title)}</h3>
                <p>${esc(message)}</p>

                <div class="nivic-confirm-actions">
                    <button type="button" class="nivic-confirm-cancel">
                        Cancelar
                    </button>

                    <button type="button" class="nivic-confirm-delete">
                        Excluir
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        const finish = (result) => {
            overlay.remove();
            document.removeEventListener('keydown', handleKeydown);
            resolve(result);
        };

        const handleKeydown = (event) => {
            if (event.key === 'Escape') {
                finish(false);
            }
        };

        overlay.querySelector('.nivic-confirm-cancel')
            .addEventListener('click', () => finish(false));

        overlay.querySelector('.nivic-confirm-delete')
            .addEventListener('click', () => finish(true));

        overlay.addEventListener('click', (event) => {
            if (event.target === overlay) {
                finish(false);
            }
        });

        document.addEventListener('keydown', handleKeydown);

        overlay.querySelector('.nivic-confirm-cancel').focus();
    });
}

let toastTimer = null;

function notify(message) {
    const element = $('#toast');

    element.textContent = message;
    element.classList.add('show');

    clearTimeout(toastTimer);

    toastTimer = setTimeout(() => {
        element.classList.remove('show');
    }, 2600);
}

async function initialize() {
    if (!token) {
        showLanding();
        return;
    }

    try {
        await loadUserAndData();
        enter(me);
    } catch (error) {
        clearSession();
        auth('login');
        notify(error.message);
    }
}

initialize();