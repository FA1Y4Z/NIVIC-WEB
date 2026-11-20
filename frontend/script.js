const API_URL = 'http://localhost:8080';
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

let token = sessionStorage.getItem(TOKEN_KEY);
let me = null;
let page = 'dashboard';
let editing = null;

let data = {
    receitas: [],
    despesas: [],
    metas: []
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
            'Não foi possível conectar ao servidor. Verifique se o Spring Boot está executando.'
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

        let message = 'Erro na comunicação com o servidor.';

        if (typeof result === 'string' && result.trim()) {
            message = result;
        } else if (result?.erro) {
            message = result.erro;
        } else if (result?.message) {
            message = result.message;
        } else if (result?.mensagem) {
            message = result.mensagem;
        } else if (result?.detail) {
            message = result.detail;
        }

        if (response.status === 400) {
            message = result?.erro || 'Dados inválidos. Confira os campos preenchidos.';
        }

        if (response.status === 404) {
            message = 'Registro ou rota não encontrado.';
        }

        if (response.status === 409) {
            message = result?.erro || 'Este cadastro já existe.';
        }

        throw new Error(message);
    }

    return result;
}

function clearSession() {
    token = null;
    me = null;

    sessionStorage.removeItem(TOKEN_KEY);

    data = {
        receitas: [],
        despesas: [],
        metas: []
    };
}

function auth(mode) {
    const login = mode === 'login';

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
        ? 'Não tem uma conta? <button type="button" id="toRegister">Cadastre-se</button>'
        : 'Já tem uma conta? <button type="button" id="toLogin">Faça login</button>';

    $('#toRegister')?.addEventListener('click', () => auth('register'));
    $('#toLogin')?.addEventListener('click', () => auth('login'));
}

function enter(user) {
    me = user;

    $('#auth').classList.add('hide');
    $('#application').classList.remove('hide');

    $('#userName').textContent = me.nome;
    $('#avatar').textContent = me.nome.trim()[0].toUpperCase();

    go('dashboard');
}

async function loadUserAndData() {
    const [user, receitas, despesas, metas] = await Promise.all([
        api('/api/usuarios/me'),
        api('/api/receitas'),
        api('/api/despesas'),
        api('/api/metas')
    ]);

    me = user;
    data.receitas = receitas;
    data.despesas = despesas;
    data.metas = metas;
}

async function refreshData() {
    const [receitas, despesas, metas] = await Promise.all([
        api('/api/receitas'),
        api('/api/despesas'),
        api('/api/metas')
    ]);

    data.receitas = receitas;
    data.despesas = despesas;
    data.metas = metas;
}

$('#loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    $('#loginError').textContent = '';

    const button = $('#loginForm button');
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

    if (senha !== confirmacao) {
        $('#regError').textContent = 'As senhas não coincidem.';
        return;
    }

    if (senha.length < 8) {
        $('#regError').textContent = 'A senha precisa ter pelo menos 8 caracteres.';
        return;
    }

    if (senha.length > 72) {
        $('#regError').textContent = 'A senha deve ter no máximo 72 caracteres.';
        return;
    }

    const button = $('#registerForm button');
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

        notify('Conta criada com sucesso!');
    } catch (error) {
        $('#regError').textContent = error.message;
    } finally {
        button.disabled = false;
    }
});

$('#logout').addEventListener('click', () => {
    clearSession();
    auth('login');
    $('#loginForm').reset();
    notify('Você saiu da sua conta.');
});

all('[data-page]').forEach(button => {
    button.addEventListener('click', () => {
        go(button.dataset.page);
    });
});

function go(p) {
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
    const inc = data.receitas.reduce(
        (sum, item) => sum + Number(item.valor || 0),
        0
    );

    const out = data.despesas.reduce(
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

function render() {
    if (!me) return;

    const pages = {
        dashboard,
        receitas: () => list('receitas'),
        despesas: () => list('despesas'),
        metas: goals,
        conta: account
    };

    $('#content').innerHTML = pages[page]();
}

function dashboard() {
    const t = totals();

    const movements = [
        ...data.receitas.map(item => ({ ...item, kind: 'Receita' })),
        ...data.despesas.map(item => ({ ...item, kind: 'Despesa' }))
    ]
        .sort((a, b) => String(b.data).localeCompare(String(a.data)))
        .slice(0, 5);

    return `
        <div class="welcome">
            <h2>Olá, ${esc(me.nome.split(' ')[0])}!</h2>
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

                ${data.metas.length
                    ? data.metas.slice(0, 4).map(goal => `
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
    const items = data[type];

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
            <input
                id="search"
                class="search"
                placeholder="⌕ Buscar por descrição ou ID..."
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
                                    `${item.id} ${item.descricao} ${detail || ''}`.toLowerCase()
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

        ${data.metas.length
            ? `
                <div class="goal-grid">
                    ${data.metas.map(goal => `
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
                    <label>Nome completo</label>
                    <input value="${esc(me.nome)}" disabled>
                </div>

                <div class="field">
                    <label>E-mail</label>
                    <input value="${esc(me.email)}" disabled>
                </div>

                <p class="muted">
                    A edição dos dados pessoais ainda não está disponível na API.
                </p>
            </section>

            <section class="profile">
                <h3>Segurança</h3>

                <p class="muted">
                    Sua conta utiliza autenticação JWT.
                    O token é removido ao encerrar a sessão.
                </p>

                <button class="secondary" id="accountLogout">
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

    const goal = type === 'metas';
    const income = type === 'receitas';

    $('#modalTitle').textContent =
        (item ? 'Editar ' : 'Nova ') +
        (goal ? 'meta' : income ? 'receita' : 'despesa');

    let fields = '';

    if (goal) {
        fields = `
            <div class="field">
                <label>Nome da meta</label>
                <input
                    name="nome"
                    required
                    value="${esc(item?.nome || '')}"
                    placeholder="Ex.: Viagem dos sonhos"
                >
            </div>

            <div class="field">
                <label>Valor desejado (R$)</label>
                <input
                    name="valor"
                    type="number"
                    min="0.01"
                    step="0.01"
                    required
                    value="${item?.valorObjetivo ?? item?.valor_objetivo ?? ''}"
                >
            </div>

            <div class="field">
                <label>Valor acumulado (R$)</label>
                <input
                    name="acumulado"
                    type="number"
                    min="0"
                    step="0.01"
                    required
                    value="${item?.valorAtual ?? item?.valor_atual ?? 0}"
                >
            </div>

            <div class="field">
                <label>Prazo</label>
                <input
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
                <label>Descrição</label>
                <input
                    name="descricao"
                    required
                    value="${esc(item?.descricao || '')}"
                    placeholder="Descrição do lançamento"
                >
            </div>

            <div class="field">
                <label>Valor (R$)</label>
                <input
                    name="valor"
                    type="number"
                    min="0.01"
                    step="0.01"
                    required
                    value="${item?.valor ?? ''}"
                >
            </div>

            <div class="field">
                <label>${income ? 'Tipo da receita' : 'Categoria da despesa'}</label>
                <input
                    name="${income ? 'tipo' : 'categoria'}"
                    required
                    value="${esc(income ? item?.tipo || '' : item?.categoria || '')}"
                    placeholder="${income ? 'Ex.: Salário, freelance...' : 'Ex.: Alimentação, transporte...'}"
                >
            </div>

            <div class="field">
                <label>Data</label>
                <input
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
}

function closeModal() {
    $('#modal').classList.add('hide');
    $('#recordForm').reset();
    editing = null;
}

$('#close').addEventListener('click', closeModal);
$('#cancel').addEventListener('click', closeModal);

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
        clearSession();
        auth('login');
        notify('Você saiu da sua conta.');
        return;
    }

    if (button.dataset.edit) {
        const type = button.dataset.type;

        const item = data[type].find(
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

    const query = event.target.value.toLowerCase();

    all('#tbody tr[data-q]').forEach(row => {
        row.classList.toggle('hide', !row.dataset.q.includes(query));
    });
});

function confirmAction(title, message) {
    return new Promise((resolve) => {
        const existing = $('#nivic-confirm-overlay');

        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.id = 'nivic-confirm-overlay';

        overlay.innerHTML = `
            <div class="nivic-confirm-card" role="dialog" aria-modal="true" aria-labelledby="nivic-confirm-title">
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

        if (!$('#nivic-confirm-styles')) {
            const style = document.createElement('style');
            style.id = 'nivic-confirm-styles';

            style.textContent = `
                #nivic-confirm-overlay {
                    position: fixed;
                    inset: 0;
                    z-index: 9999;
                    display: grid;
                    place-items: center;
                    padding: 20px;
                    background: rgba(3, 30, 43, .58);
                    backdrop-filter: blur(5px);
                    animation: nivicConfirmFade .18s ease;
                }

                .nivic-confirm-card {
                    width: min(100%, 400px);
                    padding: 28px;
                    border: 1px solid rgba(6, 43, 56, .08);
                    border-radius: 18px;
                    background: #fff;
                    color: #173746;
                    box-shadow: 0 24px 70px rgba(3, 30, 43, .24);
                    text-align: center;
                    animation: nivicConfirmEnter .2s ease;
                }

                .nivic-confirm-icon {
                    display: grid;
                    place-items: center;
                    width: 48px;
                    height: 48px;
                    margin: 0 auto 16px;
                    border-radius: 50%;
                    background: #fff0ed;
                    color: #d94b3d;
                    font-size: 25px;
                    font-weight: 700;
                }

                .nivic-confirm-card h3 {
                    margin: 0 0 9px;
                    color: #062b38;
                    font-size: 19px;
                }

                .nivic-confirm-card p {
                    margin: 0;
                    color: #647780;
                    font-size: 14px;
                    line-height: 1.6;
                }

                .nivic-confirm-actions {
                    display: flex;
                    justify-content: center;
                    gap: 10px;
                    margin-top: 25px;
                }

                .nivic-confirm-actions button {
                    min-height: 42px;
                    padding: 0 18px;
                    border: 0;
                    border-radius: 9px;
                    font: inherit;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: .18s ease;
                }

                .nivic-confirm-cancel {
                    background: #edf2f3;
                    color: #294650;
                }

                .nivic-confirm-cancel:hover {
                    background: #e1e9eb;
                }

                .nivic-confirm-delete {
                    background: #d94b3d;
                    color: #fff;
                }

                .nivic-confirm-delete:hover {
                    background: #bd392e;
                }

                @keyframes nivicConfirmFade {
                    from {
                        opacity: 0;
                    }

                    to {
                        opacity: 1;
                    }
                }

                @keyframes nivicConfirmEnter {
                    from {
                        opacity: 0;
                        transform: translateY(8px) scale(.98);
                    }

                    to {
                        opacity: 1;
                        transform: translateY(0) scale(1);
                    }
                }

                @media (max-width: 420px) {
                    .nivic-confirm-card {
                        padding: 24px 19px;
                    }

                    .nivic-confirm-actions button {
                        flex: 1;
                        padding: 0 12px;
                    }
                }
            `;

            document.head.appendChild(style);
        }

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

function notify(message) {
    const element = $('#toast');

    element.textContent = message;
    element.classList.add('show');

    setTimeout(() => {
        element.classList.remove('show');
    }, 2600);
}

async function initialize() {
    if (!token) {
        auth('login');
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