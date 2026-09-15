const state = {
  albuns: [], categorias: [], produtos: [], clientes: [], configuracoes: [],
  precos: [], regras: [], orcamentos: [], vendas: [], tiposCustomizados: [],
  compras: [], catalogos: [], usuario: null
};

const produtoPage = { number: 0, size: 20, totalPages: 0, totalElements: 0, content: [], sort: 'numero', dir: 'asc' };
const historicoPage = { number: 0, size: 20, totalPages: 0, totalElements: 0, content: [], sort: 'data', dir: 'desc' };
const localSort = {};
const edit = { produto:null, album:null, categoria:null, preco:null, cliente:null, config:null, compra:null, tipo:null };
const vendaItensSelecionados = [];
const orcamentoItensSelecionados = [];
const listasInterpretadas = { venda: [], orcamento: [] };
const catalogoGridState = { chave:null, itens:[], selecionados:new Set() };
const dashboardPages = {volume:0, mais:0, estoque:0};
const itemPages = {venda:0, orcamento:0};
const previewPages = {venda:0, orcamento:0};
const previewResultados = {venda:[], orcamento:[]};

const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];
const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c]));
const moeda = v => Number(v || 0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'});
const val = s => $(s)?.value ?? '';
const num = s => val(s)==='' ? null : Number(val(s));

async function api(url, options={}) {
  const response = await fetch(url, {
    headers: {'Content-Type':'application/json'},
    ...options
  });
  if (response.status === 401) {
    location.href = '/login.html';
    throw new Error('Sessão expirada.');
  }
  if (!response.ok) {
    let msg = `Erro ${response.status}`;
    try {
      const data = await response.json();
      msg = data.message || data.error || msg;
    } catch {}
    throw new Error(msg);
  }
  if (response.status === 204) return null;
  return response.json();
}

function toast(msg) {
  const el = $('#toast');
  el.textContent = msg;
  el.classList.add('show');
  setTimeout(()=>el.classList.remove('show'), 2800);
}

function tipoAlbumLabel(v) {
  return ({
    COPA_DO_MUNDO:'Copa do Mundo',
    BRASILEIRAO:'Brasileirão',
    CHAMPIONS:'Champions',
    OUTRO:'Outro'
  })[v] || v || 'Outro';
}

function tipoProdutoLabel(produto) {
  if (produto?.tipoProdutoCustomizado) return produto.tipoProdutoCustomizado.nome;
  return ({
    FIGURINHA:'Figurinha',
    ALBUM_COMPLETO:'Álbum completo',
    ALBUM_INCOMPLETO:'Álbum incompleto',
    CUSTOMIZADO:'Personalizado'
  })[produto?.tipoProduto] || produto?.tipoProduto || 'Produto';
}

function produtoTipoValue(produto) {
  return produto?.tipoProdutoCustomizado
    ? `CUSTOM:${produto.tipoProdutoCustomizado.codigoTipoProduto}`
    : produto?.tipoProduto;
}

function albumDescricao(album) {
  if (!album) return '—';
  return `${album.nomeAlbum || tipoAlbumLabel(album.tipoAlbum)}${album.ano ? ` ${album.ano}` : ''}`;
}

function itemIdentificacao(produto) {
  if (!produto) return 'Produto';
  if (produto.tipoProduto === 'FIGURINHA') {
    const prefixo = produto.selecaoFigurinha ? produto.selecaoFigurinha : '';
    return `${prefixo}${produto.numeroFigurinha ?? ''}${produto.nomeProduto ? ` — ${produto.nomeProduto}` : ''}` || 'Figurinha';
  }
  return produto.nomeProduto || tipoProdutoLabel(produto);
}

function uniqueSorted(values, numeric=false) {
  const arr = [...new Set(values.filter(v => v !== null && v !== undefined && v !== ''))];
  return arr.sort(numeric ? (a,b)=>Number(a)-Number(b) : (a,b)=>String(a).localeCompare(String(b),'pt-BR'));
}

function setSelect(selector, options, placeholder='Selecione') {
  const select = $(selector);
  if (!select) return;
  const old = select.value;
  select.innerHTML = `<option value="">${esc(placeholder)}</option>` +
    options.map(o=>`<option value="${esc(o.value)}">${esc(o.label)}</option>`).join('');
  if ([...select.options].some(o=>o.value===old)) select.value = old;
  syncSearchableSelect(select);
}

/* ---------- Select pesquisável para filtros ---------- */
function enhanceSearchableSelect(select) {
  if (!select || select.dataset.searchEnhanced === '1') {
    if (select) syncSearchableSelect(select);
    return;
  }
  select.dataset.searchEnhanced = '1';

  const wrapper = document.createElement('div');
  wrapper.className = 'searchable-filter';
  select.parentNode.insertBefore(wrapper, select);
  wrapper.appendChild(select);
  select.style.display = 'none';

  const input = document.createElement('input');
  input.type = 'text';
  input.autocomplete = 'off';

  const dropdown = document.createElement('div');
  dropdown.className = 'searchable-dropdown hidden';

  wrapper.appendChild(input);
  wrapper.appendChild(dropdown);

  function renderDropdown() {
    const term = input.value.trim().toLowerCase();
    const options = [...select.options].filter(o =>
      !term || o.textContent.toLowerCase().includes(term)
    );
    dropdown.innerHTML = options.map(o =>
      `<div class="searchable-option ${o.value===select.value?'selected':''}" data-value="${esc(o.value)}">${esc(o.textContent)}</div>`
    ).join('') || `<div class="searchable-option">Nenhum resultado</div>`;
    dropdown.classList.remove('hidden');

    dropdown.querySelectorAll('[data-value]').forEach(item => {
      item.onclick = () => {
        select.value = item.dataset.value;
        const opt = select.selectedOptions[0];
        input.value = select.value ? opt?.textContent || '' : '';
        dropdown.classList.add('hidden');
        select.dispatchEvent(new Event('change',{bubbles:true}));
      };
    });
  }

  input.addEventListener('focus', renderDropdown);
  input.addEventListener('input', renderDropdown);
  wrapper.addEventListener('click', e => e.stopPropagation());
  document.addEventListener('click', ()=>dropdown.classList.add('hidden'));
  syncSearchableSelect(select);
}

function syncSearchableSelect(select) {
  if (!select || select.dataset.searchEnhanced !== '1') return;
  const wrapper = select.closest('.searchable-filter');
  const input = wrapper?.querySelector('input');
  if (!input) return;
  const selected = select.selectedOptions[0];
  input.value = select.value ? selected?.textContent || '' : '';
  input.placeholder = select.options[0]?.textContent || 'Todos';
}

function enhanceAllSearchableFilters() {
  $$('.filter-search-select').forEach(enhanceSearchableSelect);
}

function updateFilterButton(panel) {
  if (!panel) return;
  const button = $(`[data-filter-target="${panel.id}"]`);
  if (!button) return;
  const count = [...panel.querySelectorAll('input,select')].filter(el => String(el.value || '').trim() !== '').length;
  button.classList.toggle('filter-active', count > 0);
  button.textContent = count ? `⏷ Filtrar (${count})` : '⏷ Filtrar';
}

$$('.filter-toggle').forEach(btn => {
  btn.onclick = () => {
    const panel = document.getElementById(btn.dataset.filterTarget);
    panel?.classList.toggle('hidden');
  };
});

/* ---------- Navegação ---------- */
function showView(id) {
  $$('.view').forEach(v=>v.classList.remove('active'));
  document.getElementById(id)?.classList.add('active');
  closeSideMenu();
}

$$('[data-view]').forEach(btn => btn.onclick = ()=>showView(btn.dataset.view));

function openSideMenu() {
  $('#sideMenu').classList.add('open');
  $('#menuOverlay').classList.remove('hidden');
}
function closeSideMenu() {
  $('#sideMenu').classList.remove('open');
  $('#menuOverlay').classList.add('hidden');
}
$('#abrirMenu').onclick = openSideMenu;
$('#fecharMenu').onclick = closeSideMenu;
$('#menuOverlay').onclick = closeSideMenu;

$$('[data-sales-tab]').forEach(btn => {
  btn.onclick = () => {
    $$('.sales-tab').forEach(p=>p.classList.remove('active-sales-tab'));
    $$('.tab-btn').forEach(b=>b.classList.remove('active-tab'));
    document.getElementById(btn.dataset.salesTab)?.classList.add('active-sales-tab');
    btn.classList.add('active-tab');
    if (btn.dataset.salesTab === 'historicoVenda') carregarHistorico(0);
    if (btn.dataset.salesTab === 'produtosVendidosVenda') carregarProdutosVendidos();
  };
});

/* ---------- Usuário e preferências ---------- */
async function carregarUsuario() {
  const u = await api('/auth/me');
  state.usuario = u;
  $('#usuarioNome').textContent = u.nome;
  $('#limiteEstoqueBaixo').value = u.limiteEstoqueBaixo ?? 3;
}
$('#btnPreferencias').onclick = ()=>$('#preferenciasModal').classList.remove('hidden');
$('#fecharPreferencias').onclick = ()=>$('#preferenciasModal').classList.add('hidden');
$('#formPreferencias').onsubmit = async e => {
  e.preventDefault();
  const limite = Number($('#limiteEstoqueBaixo').value);
  await api('/auth/preferencias',{method:'PUT',body:JSON.stringify({limiteEstoqueBaixo:limite})});
  $('#preferenciasModal').classList.add('hidden');
  await carregarUsuario();
  await carregarDashboard();
};

/* ---------- Carregamento geral ---------- */
async function loadAll() {
  try {
    const [albuns,categorias,produtos,clientes,configs,precos,regras,orcamentos,vendas,tipos,compras,catalogos] = await Promise.all([
      api('/albuns'), api('/categorias'), api('/produtos'), api('/clientes'),
      api('/configuracoes-venda'), api('/precos-categoria-album'), api('/regras-shopee'),
      api('/orcamentos'), api('/vendas'), api('/tipos-produto'), api('/compras-produtos'), api('/catalogos')
    ]);

    Object.assign(state,{
      albuns,categorias,produtos,clientes,configuracoes:configs,precos,regras,
      orcamentos,vendas,tiposCustomizados:tipos,compras,catalogos
    });

    await Promise.all(state.orcamentos.map(async o => {
      try { o._total = await api(`/orcamentos/${o.codigoOrcamento}/total`); }
      catch { o._total = 0; }
    }));

    await Promise.all(state.vendas.filter(v=>v.statusVenda==='EM_ABERTO').map(async v => {
      try { v._resumo = await api(`/vendas/${v.codigoVenda}/resumo`); } catch {}
    }));

    refreshSelects();
    buildPicker('venda');
    buildPicker('orcamento');
    renderAll();
    await carregarPaginaProdutos(0);
    await carregarHistorico(0);
    await carregarDashboard();
  } catch (e) {
    console.error(e);
    toast(e.message);
  }
}

async function carregarDashboard() {
  try {
    const params = new URLSearchParams();
    const inicio = val('#dashboardInicio');
    const fim = val('#dashboardFim');
    if (inicio) params.set('inicio', inicio);
    if (fim) params.set('fim', fim);
    const d = await api(`/dashboard${params.toString() ? `?${params}` : ''}`);
    $('#dFat').textContent = moeda(d.faturamento); $('#dLucro').textContent = moeda(d.lucroLiquido);
    $('#dCompras').textContent = moeda(d.comprasProdutos); $('#dTaxas').textContent = moeda(d.taxasDespesas);
    $('#dTicket').textContent = moeda(d.ticketMedio); $('#dFinalizadas').textContent = d.vendasFinalizadas;
    renderDashboardPage('#dVolume', d.volumePorTipo||[], 5, 'volume', x=>`<tr><td>${esc(x.tipo)}</td><td>${x.quantidade}</td><td>${moeda(x.faturamento)}</td></tr>`, '<tr><th>Tipo</th><th>Qtd.</th><th>Faturamento</th></tr>');
    renderDashboardPage('#dMais', d.maisVendidos||[], 5, 'mais', x=>`<tr><td>${esc(x.produto)}</td><td>${x.quantidade}</td></tr>`, '<tr><th>Produto</th><th>Qtd.</th></tr>');
    renderDashboardPage('#dEstoque', d.estoqueBaixo||[], 10, 'estoque', x=>`<tr><td>${esc(x.produto)}</td><td>${x.estoque}</td></tr>`, '<tr><th>Produto</th><th>Estoque</th></tr>');
    $('#dStatus').innerHTML = `<div><b>Em aberto:</b> ${d.vendasEmAberto}</div><div><b>Canceladas:</b> ${d.vendasCanceladas}</div>`;
  } catch(e) { toast(e.message); }
}
function renderDashboardPage(selector, arr, size, key, rowFn, head){
  const totalPages=Math.max(1,Math.ceil(arr.length/size)); dashboardPages[key]=Math.min(dashboardPages[key],totalPages-1);
  const ini=dashboardPages[key]*size, page=arr.slice(ini,ini+size);
  $(selector).innerHTML=`<table><thead>${head}</thead><tbody>${page.map(rowFn).join('')}</tbody></table><div class="pagination"><button ${dashboardPages[key]===0?'disabled':''} onclick="mudarDashboardPagina('${key}',-1)">‹</button><span>${dashboardPages[key]+1}/${totalPages}</span><button ${dashboardPages[key]>=totalPages-1?'disabled':''} onclick="mudarDashboardPagina('${key}',1)">›</button></div>`;
  $(selector).dataset.items=JSON.stringify(arr); $(selector).dataset.size=size;
}
window.mudarDashboardPagina=(key,delta)=>{dashboardPages[key]=Math.max(0,dashboardPages[key]+delta);carregarDashboard()};

/* ---------- Selects ---------- */
function tiposProdutoOptions() {
  return [
    {value:'FIGURINHA',label:'Figurinha'},
    {value:'ALBUM_COMPLETO',label:'Álbum completo'},
    {value:'ALBUM_INCOMPLETO',label:'Álbum incompleto'},
    ...state.tiposCustomizados.map(t=>({value:`CUSTOM:${t.codigoTipoProduto}`,label:t.nome}))
  ];
}

function refreshSelects() {
  setSelect('#produtoTipo', tiposProdutoOptions(), 'Selecione');
  setSelect('#produtoAlbum', state.albuns.map(a=>({value:a.codigoAlbum,label:albumDescricao(a)})), 'Selecione o álbum');
  setSelect('#produtoCategoria', state.categorias.map(c=>({value:c.codigoCategoria,label:c.nomeCategoria})), 'Selecione a categoria');

  setSelect('#precoAlbum', state.albuns.map(a=>({value:a.codigoAlbum,label:albumDescricao(a)})), 'Selecione o álbum');
  setSelect('#precoCategoria', state.categorias.map(c=>({value:c.codigoCategoria,label:c.nomeCategoria})), 'Selecione a categoria');

  ['#vendaCliente','#orcamentoCliente'].forEach(id =>
    setSelect(id,state.clientes.map(c=>({value:c.codigoCliente,label:c.nomeCliente})),'Sem cliente')
  );
  setSelect('#vendaConfiguracao',state.configuracoes.map(c=>({value:c.codigoConfiguracao,label:c.nome})),'Selecione');
  setSelect('#converterConfiguracao',state.configuracoes.map(c=>({value:c.codigoConfiguracao,label:c.nome})),'Selecione');

  const listaAlbunsFig = state.albuns.map(a=>({value:a.codigoAlbum,label:albumDescricao(a)}));
  setSelect('#vendaListaAlbum',listaAlbunsFig,'Selecione o álbum');
  setSelect('#orcListaAlbum',listaAlbunsFig,'Selecione o álbum');

  setSelect('#filtroProdutoTipo',tiposProdutoOptions(),'Todos');
  setSelect('#filtroProdutoAlbum',uniqueSorted(state.albuns.map(a=>a.nomeAlbum)).map(v=>({value:v,label:v})),'Todos');
  setSelect('#filtroProdutoAno',uniqueSorted(state.albuns.map(a=>a.ano),true).map(v=>({value:v,label:v})),'Todos');
  setSelect('#filtroProdutoSelecao',uniqueSorted(state.produtos.map(p=>p.selecaoFigurinha)).map(v=>({value:v,label:v})),'Todas');
  setSelect('#filtroProdutoCategoria',state.categorias.map(c=>({value:c.codigoCategoria,label:c.nomeCategoria})),'Todas');

  setSelect('#filtroAlbumAno',uniqueSorted(state.albuns.map(a=>a.ano),true).map(v=>({value:v,label:v})),'Todos');
  setSelect('#filtroAlbumTipo',uniqueSorted(state.albuns.map(a=>a.tipoAlbum)).map(v=>({value:v,label:tipoAlbumLabel(v)})),'Todos');

  setSelect('#filtroPrecoAlbum',state.albuns.map(a=>({value:a.codigoAlbum,label:albumDescricao(a)})),'Todos');
  setSelect('#filtroPrecoAno',uniqueSorted(state.albuns.map(a=>a.ano),true).map(v=>({value:v,label:v})),'Todos');
  setSelect('#filtroPrecoCategoria',state.categorias.map(c=>({value:c.codigoCategoria,label:c.nomeCategoria})),'Todas');

  setSelect('#filtroOrcamentoCliente',state.clientes.map(c=>({value:c.codigoCliente,label:c.nomeCliente})),'Todos');
  setSelect('#historicoCliente',state.clientes.map(c=>({value:c.codigoCliente,label:c.nomeCliente})),'Todos');
  setSelect('#historicoCanal',state.configuracoes.map(c=>({value:c.codigoConfiguracao,label:c.nome})),'Todos');
  setSelect('#historicoTipoProduto',tiposProdutoOptions(),'Todos');
  setSelect('#produtosVendidosTipo',tiposProdutoOptions(),'Todos');
  setSelect('#produtosVendidosAlbum',state.albuns.map(a=>({value:a.codigoAlbum,label:albumDescricao(a)})),'Todos');
  setSelect('#produtosVendidosAno',uniqueSorted(state.albuns.map(a=>a.ano),true).map(v=>({value:v,label:v})),'Todos');
  setSelect('#produtosVendidosSelecao',uniqueSorted(state.produtos.map(p=>p.selecaoFigurinha)).map(v=>({value:v,label:v})),'Todas');

  enhanceAllSearchableFilters();
  $$('.table-filters,.history-filters').forEach(updateFilterButton);
}

/* ---------- Ordenação local ---------- */
const sortFields = {
  albuns:{nome:x=>x.nomeAlbum,ano:x=>x.ano||0,tipo:x=>x.tipoAlbum||''},
  categorias:{nome:x=>x.nomeCategoria},
  clientes:{nome:x=>x.nomeCliente,email:x=>x.emailCliente||'',telefone:x=>x.telefoneCliente||''},
  precos:{album:x=>x.album?.nomeAlbum||'',ano:x=>x.album?.ano||0,categoria:x=>x.categoria?.nomeCategoria||'',valor:x=>Number(x.valor||0)},
  configs:{nome:x=>x.nome,emb:x=>Number(x.embalagem||0),shopee:x=>x.configuracaoShopee?1:0},
  regras:{valor:x=>Number(x.valorItem||0),taxa:x=>Number(x.taxaTotal||0)},
  orcamentos:{data:x=>x.data||'',cliente:x=>x.cliente?.nomeCliente||'',total:x=>Number(x._total||0),status:x=>x.statusOrcamento||'EM_ABERTO'},
  compras:{data:x=>x.data||'',descricao:x=>x.descricao||'',valor:x=>Number(x.valorTotal||0)}
};

function sortedCopy(key, rows) {
  const s = localSort[key];
  if (!s) return [...rows];
  const getter = sortFields[key]?.[s.field];
  if (!getter) return [...rows];
  return [...rows].sort((a,b)=>{
    let x=getter(a),y=getter(b);
    if(typeof x==='string'){x=x.toLowerCase();y=String(y).toLowerCase()}
    return (x<y?-1:x>y?1:0)*s.dir;
  });
}

window.sortLocalTable = (key,field) => {
  const current = localSort[key] || {};
  localSort[key] = {field,dir:current.field===field?-current.dir:1};
  renderAll();
};

function thLocal(key,label,field) {
  const s=localSort[key];
  const arrow=s?.field===field?(s.dir===1?' ↑':' ↓'):' ⇅';
  return `<th class="sortable" onclick="sortLocalTable('${key}','${field}')">${label}<span class="sort-indicator">${arrow}</span></th>`;
}

/* ---------- Render tabelas gerais ---------- */
function renderAll() {
  renderAlbuns();
  renderCategorias();
  renderClientes();
  renderPrecos();
  renderConfiguracoes();
  renderRegras();
  renderCompras();
  renderOrcamentos();
  renderVendasAbertas();
  renderTiposProduto();
  renderCatalogos();
  refreshSelects();
}

function renderAlbuns() {
  const busca = val('#filtroAlbumNome').trim().toLowerCase();
  const ano = val('#filtroAlbumAno');
  const tipo = val('#filtroAlbumTipo');
  let rows = state.albuns.filter(a =>
    (!busca || (a.nomeAlbum||'').toLowerCase().includes(busca)) &&
    (!ano || String(a.ano)===ano) &&
    (!tipo || a.tipoAlbum===tipo)
  );
  rows=sortedCopy('albuns',rows);
  $('#listaAlbuns').innerHTML=`<table><thead><tr>${thLocal('albuns','Nome','nome')}${thLocal('albuns','Ano','ano')}${thLocal('albuns','Tipo','tipo')}<th>Ações</th></tr></thead><tbody>${
    rows.map(a=>`<tr><td>${esc(a.nomeAlbum)}</td><td>${a.ano??'—'}</td><td>${esc(tipoAlbumLabel(a.tipoAlbum))}</td><td><button onclick="editarAlbum(${a.codigoAlbum})">Editar</button> <button class="danger" onclick="deletar('/albuns/${a.codigoAlbum}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}

function renderCategorias() {
  const busca=val('#filtroCategoriaNome').trim().toLowerCase();
  let rows=state.categorias.filter(c=>!busca||(c.nomeCategoria||'').toLowerCase().includes(busca));
  rows=sortedCopy('categorias',rows);
  $('#listaCategorias').innerHTML=`<table><thead><tr>${thLocal('categorias','Nome','nome')}<th>Ações</th></tr></thead><tbody>${
    rows.map(c=>`<tr><td>${esc(c.nomeCategoria)}</td><td><button onclick="editarCategoria(${c.codigoCategoria})">Editar</button> <button class="danger" onclick="deletar('/categorias/${c.codigoCategoria}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}

function renderClientes() {
  const busca=val('#filtroClienteBusca').trim().toLowerCase();
  let rows=state.clientes.filter(c=>!busca||[c.nomeCliente,c.emailCliente,c.telefoneCliente].some(v=>(v||'').toLowerCase().includes(busca)));
  rows=sortedCopy('clientes',rows);
  $('#listaClientes').innerHTML=`<table><thead><tr>${thLocal('clientes','Nome','nome')}${thLocal('clientes','E-mail','email')}${thLocal('clientes','Telefone','telefone')}<th>Ações</th></tr></thead><tbody>${
    rows.map(c=>`<tr><td>${esc(c.nomeCliente)}</td><td>${esc(c.emailCliente||'—')}</td><td>${esc(c.telefoneCliente||'—')}</td><td><button onclick="editarCliente(${c.codigoCliente})">Editar</button> <button class="danger" onclick="deletar('/clientes/${c.codigoCliente}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}

function renderPrecos() {
  const album=val('#filtroPrecoAlbum'),ano=val('#filtroPrecoAno'),cat=val('#filtroPrecoCategoria');
  let rows=state.precos.filter(p=>
    (!album||String(p.album?.codigoAlbum)===album) &&
    (!ano||String(p.album?.ano)===ano) &&
    (!cat||String(p.categoria?.codigoCategoria)===cat)
  );
  rows=sortedCopy('precos',rows);
  $('#listaPrecos').innerHTML=`<table><thead><tr>${thLocal('precos','Álbum','album')}${thLocal('precos','Ano','ano')}${thLocal('precos','Categoria','categoria')}${thLocal('precos','Valor','valor')}<th>Ações</th></tr></thead><tbody>${
    rows.map(p=>`<tr><td>${esc(p.album?.nomeAlbum||'—')}</td><td>${p.album?.ano??'—'}</td><td>${esc(p.categoria?.nomeCategoria||'—')}</td><td>${moeda(p.valor)}</td><td><button onclick="editarPreco(${p.codigoPreco})">Editar</button> <button class="danger" onclick="deletar('/precos-categoria-album/${p.codigoPreco}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}

function renderConfiguracoes() {
  const busca=val('#filtroConfiguracaoNome').trim().toLowerCase(),shopee=val('#filtroConfiguracaoShopee');
  let rows=state.configuracoes.filter(c=>
    (!busca||(c.nome||'').toLowerCase().includes(busca)) &&
    (!shopee||String(c.configuracaoShopee)===shopee)
  );
  rows=sortedCopy('configs',rows);
  $('#listaConfiguracoes').innerHTML=`<table><thead><tr>${thLocal('configs','Nome','nome')}${thLocal('configs','Embalagem','emb')}${thLocal('configs','Shopee','shopee')}<th>Vendedor</th><th>CPF &gt; 450</th><th>Ações</th></tr></thead><tbody>${
    rows.map(c=>`<tr><td>${esc(c.nome)}</td><td>${moeda(c.embalagem)}</td><td>${c.configuracaoShopee?'Sim':'Não'}</td><td>${esc(c.tipoVendedorShopee||'—')}</td><td>${c.tipoVendedorShopee==='CPF'&&c.cpfAcima450Pedidos?'Sim':'Não'}</td><td><button onclick="editarConfiguracao(${c.codigoConfiguracao})">Editar</button> <button class="danger" onclick="deletar('/configuracoes-venda/${c.codigoConfiguracao}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}

function renderRegras() {
  const f=val('#filtroRegraShopee');
  let rows=state.regras.filter(r=>!f||Number(r.valorItem)===Number(f));
  rows=sortedCopy('regras',rows);
  $('#listaRegrasShopee').innerHTML=`<table><thead><tr>${thLocal('regras','Valor total da venda','valor')}${thLocal('regras','Taxa total','taxa')}<th>Ações</th></tr></thead><tbody>${
    rows.map(r=>`<tr><td>${moeda(r.valorItem)}</td><td>${moeda(r.taxaTotal)}</td><td><button class="danger" onclick="deletar('/regras-shopee/${r.codigoRegraShopee}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}

function renderCompras() {
  const busca=val('#filtroCompraDescricao').trim().toLowerCase(),data=val('#filtroCompraData');
  let rows=state.compras.filter(c=>
    (!busca||(c.descricao||'').toLowerCase().includes(busca)) &&
    (!data||c.data===data)
  );
  rows=sortedCopy('compras',rows);
  $('#listaCompras').innerHTML=`<table><thead><tr>${thLocal('compras','Data','data')}${thLocal('compras','Descrição','descricao')}${thLocal('compras','Valor','valor')}<th>Observação</th><th>Ações</th></tr></thead><tbody>${
    rows.map(c=>`<tr><td>${esc(c.data||'—')}</td><td>${esc(c.descricao)}</td><td>${moeda(c.valorTotal)}</td><td>${esc(c.observacao||'—')}</td><td><button onclick="editarCompra(${c.codigoCompra})">Editar</button> <button class="danger" onclick="deletar('/compras-produtos/${c.codigoCompra}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}

function renderOrcamentos() {
  const cliente=val('#filtroOrcamentoCliente'),data=val('#filtroOrcamentoData'),min=num('#filtroOrcamentoMin'),max=num('#filtroOrcamentoMax');
  let rows=state.orcamentos.filter(o=>
    (!cliente||String(o.cliente?.codigoCliente)===cliente) &&
    (!data||o.data===data) &&
    (min===null||Number(o._total)>=min) &&
    (max===null||Number(o._total)<=max)
  );
  rows=sortedCopy('orcamentos',rows);
  $('#listaOrcamentos').innerHTML=`<table><thead><tr>${thLocal('orcamentos','Data','data')}${thLocal('orcamentos','Cliente','cliente')}${thLocal('orcamentos','Valor total','total')}${thLocal('orcamentos','Status','status')}<th>Ações</th></tr></thead><tbody>${
    rows.map(o=>{
      const status=o.statusOrcamento||'EM_ABERTO';
      const convertido=status==='CONVERTIDO'||o.vendaConvertida;
      return `<tr><td>${esc(o.data||'—')}</td><td>${esc(o.cliente?.nomeCliente||'Sem cliente')}</td><td>${moeda(o._total)}</td><td><span class="badge">${esc(status)}</span></td><td><button onclick="verOrcamento(${o.codigoOrcamento})">Ver</button> ${!convertido?`<button class="success" onclick="abrirConverterOrcamento(${o.codigoOrcamento})">Converter</button> <button class="danger" onclick="deletar('/orcamentos/${o.codigoOrcamento}')">Excluir</button>`:`${o.vendaConvertida?.codigoVenda?`<button onclick="verVenda(${o.vendaConvertida.codigoVenda})">Ver venda</button>`:''}`}</td></tr>`;
    }).join('')
  }</tbody></table>`;
}

function renderVendasAbertas() {
  const rows=state.vendas.filter(v=>v.statusVenda==='EM_ABERTO');
  $('#listaVendasAbertas').innerHTML=`<table><thead><tr><th>Data</th><th>Cliente</th><th>Canal</th><th>Faturamento</th><th>Ações</th></tr></thead><tbody>${
    rows.map(v=>`<tr><td>${esc(v.data||'—')}</td><td>${esc(v.cliente?.nomeCliente||'Sem cliente')}</td><td>${esc(v.configuracaoVenda?.nome||'—')}</td><td>${moeda(v._resumo?.faturamento)}</td><td><button onclick="verVenda(${v.codigoVenda})">Detalhes</button> <button class="success" onclick="postAction('/vendas/${v.codigoVenda}/finalizar')">Finalizar</button> <button class="secondary" onclick="postAction('/vendas/${v.codigoVenda}/cancelar')">Cancelar</button> <button class="danger" onclick="deletar('/vendas/${v.codigoVenda}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
}


/* ---------- Catálogos pré-carregados ---------- */
function renderCatalogos() {
  const box = $('#listaCatalogos');
  if (!box) return;
  if (!state.catalogos.length) {
    box.innerHTML = '<div class="empty">Nenhum catálogo pré-carregado disponível.</div>';
    return;
  }
  box.innerHTML = state.catalogos.map(c => `
    <article class="catalog-card">
      <h3>${esc(c.nome)}</h3>
      <div class="catalog-meta">${esc(tipoAlbumLabel(c.tipoAlbum))} · ${c.ano} · ${c.quantidadeItens} figurinhas</div>
      <div class="catalog-actions">
        <button type="button" onclick="importarCatalogo('${esc(c.chave)}')">${c.importado?'Atualizar catálogo no estoque':'Adicionar ao meu estoque'}</button>
        <button type="button" class="secondary" onclick="abrirGridCatalogo('${esc(c.chave)}')" ${c.importado?'':'disabled'}>Gerenciar categorias</button>
      </div>
      ${c.importado?'<span class="badge">Importado</span>':'<span class="muted">Importe para classificar e usar no estoque.</span>'}
    </article>
  `).join('');
}

window.importarCatalogo = async chave => {
  if (!confirm('Adicionar as figurinhas deste catálogo ao seu estoque com estoque 0 e categoria Comum?')) return;
  try {
    const r = await api(`/catalogos/${encodeURIComponent(chave)}/importar`, {method:'POST'});
    toast(`${r.criados} figurinha(s) adicionada(s). ${r.jaExistentes} já existiam.`);
    await loadAll();
  } catch(e) { toast(e.message); }
};

window.abrirGridCatalogo = async chave => {
  try {
    catalogoGridState.chave = chave;
    catalogoGridState.selecionados.clear();
    catalogoGridState.itens = await api(`/catalogos/${encodeURIComponent(chave)}/itens`);
    const catalogo = state.catalogos.find(c=>c.chave===chave);
    $('#catalogoGridTitulo').textContent = `Categorias — ${catalogo?.nome || chave}`;
    $('#catalogoCategoriasBox').classList.remove('hidden');
    preencherFiltrosCatalogo();
    renderCatalogoGrid();
    $('#catalogoCategoriasBox').scrollIntoView({behavior:'smooth',block:'start'});
  } catch(e) { toast(e.message); }
};

function preencherFiltrosCatalogo() {
  const itens = catalogoGridState.itens;
  setSelect('#catalogoFiltroGrupo', uniqueSorted(itens.map(i=>i.prefixo)).map(prefixo=>{
    const it=itens.find(x=>x.prefixo===prefixo);
    return {value:prefixo,label:`${prefixo} — ${it?.grupoNome || prefixo}`};
  }), 'Todos');
  const categoriasAtuais = uniqueSorted(itens.map(i=>i.categoria));
  setSelect('#catalogoFiltroCategoria', categoriasAtuais.map(v=>({value:v,label:v})), 'Todas');
  setSelect('#catalogoCategoriaAplicar', state.categorias.map(c=>({value:c.codigoCategoria,label:c.nomeCategoria})), 'Selecione a categoria');
  enhanceAllSearchableFilters();
}

function catalogoItensFiltrados() {
  const grupo=val('#catalogoFiltroGrupo');
  const categoria=val('#catalogoFiltroCategoria');
  const busca=val('#catalogoFiltroBusca').trim().toUpperCase();
  return catalogoGridState.itens.filter(i=>
    (!grupo||i.prefixo===grupo) &&
    (!categoria||(i.categoria||'')===categoria) &&
    (!busca||i.codigo.toUpperCase().includes(busca))
  );
}

function categoriaCorClass(codigoCategoria, nomeCategoria='') {
  if (!codigoCategoria || String(nomeCategoria).toLowerCase()==='comum') return 'cat-0';
  return `cat-${((Number(codigoCategoria)-1)%7)+1}`;
}

function renderCatalogoGrid() {
  const box=$('#catalogoGrid');
  if(!box)return;
  const itens=catalogoItensFiltrados();
  const grupos=new Map();
  itens.forEach(i=>{
    if(!grupos.has(i.prefixo))grupos.set(i.prefixo,{nome:i.grupoNome,itens:[]});
    grupos.get(i.prefixo).itens.push(i);
  });
  box.innerHTML=[...grupos.entries()].map(([prefixo,g])=>`
    <div class="catalog-group">
      <h4>${esc(g.nome)} <span class="muted">(${esc(prefixo)})</span></h4>
      <div class="catalog-grid">
        ${g.itens.map(i=>`<button type="button" class="catalog-sticker ${categoriaCorClass(i.codigoCategoria,i.categoria)} ${catalogoGridState.selecionados.has(i.codigoProduto)?'selected':''}" data-produto="${i.codigoProduto||''}" title="${esc(i.categoria||'Comum')}">${esc(i.codigo)}<span class="cat-dot"></span></button>`).join('')}
      </div>
    </div>
  `).join('') || '<div class="empty">Nenhuma figurinha encontrada com esses filtros.</div>';
  box.querySelectorAll('.catalog-sticker[data-produto]').forEach(b=>{
    b.disabled=!b.dataset.produto;
    b.onclick=()=>{
      const id=Number(b.dataset.produto);
      if(!id)return;
      if(catalogoGridState.selecionados.has(id))catalogoGridState.selecionados.delete(id);
      else catalogoGridState.selecionados.add(id);
      renderCatalogoGrid();
    };
  });
  $('#catalogoSelecionadas').textContent=`${catalogoGridState.selecionados.size} selecionada(s)`;
  renderCatalogoLegenda();
}

function renderCatalogoLegenda() {
  const cats=[...new Map(catalogoGridState.itens.filter(i=>i.categoria).map(i=>[i.codigoCategoria,{codigo:i.codigoCategoria,nome:i.categoria}])).values()];
  $('#catalogoLegenda').innerHTML=cats.map(c=>`<span class="catalog-legend-item"><span class="catalog-legend-dot ${categoriaCorClass(c.codigo,c.nome)}"></span>${esc(c.nome)}</span>`).join('');
}

$('#catalogoFiltroGrupo')?.addEventListener('change',renderCatalogoGrid);
$('#catalogoFiltroCategoria')?.addEventListener('change',renderCatalogoGrid);
$('#catalogoFiltroBusca')?.addEventListener('input',renderCatalogoGrid);
$('#catalogoSelecionarFiltrados')?.addEventListener('click',()=>{
  catalogoItensFiltrados().forEach(i=>{if(i.codigoProduto)catalogoGridState.selecionados.add(i.codigoProduto)});
  renderCatalogoGrid();
});
$('#catalogoLimparSelecao')?.addEventListener('click',()=>{catalogoGridState.selecionados.clear();renderCatalogoGrid()});
$('#fecharCatalogoGrid')?.addEventListener('click',()=>$('#catalogoCategoriasBox').classList.add('hidden'));
$('#catalogoAplicarCategoria')?.addEventListener('click',async()=>{
  const codigoCategoria=num('#catalogoCategoriaAplicar');
  const codigosProdutos=[...catalogoGridState.selecionados];
  if(!codigoCategoria)return toast('Selecione a categoria a aplicar.');
  if(!codigosProdutos.length)return toast('Selecione pelo menos uma figurinha.');
  try{
    const r=await api(`/catalogos/${encodeURIComponent(catalogoGridState.chave)}/categorias`,{method:'PUT',body:JSON.stringify({codigoCategoria,codigosProdutos})});
    toast(`${r.atualizados} figurinha(s) alterada(s) para ${r.categoria}.`);
    catalogoGridState.selecionados.clear();
    catalogoGridState.itens=await api(`/catalogos/${encodeURIComponent(catalogoGridState.chave)}/itens`);
    await loadAll();
    preencherFiltrosCatalogo();
    renderCatalogoGrid();
  }catch(e){toast(e.message)}
});

/* ---------- Produtos paginados ---------- */
function produtoQuery(page=produtoPage.number) {
  const p=new URLSearchParams({page:String(page),size:'20',ordenarPor:produtoPage.sort,direcao:produtoPage.dir});
  const map=[
    ['tipo','#filtroProdutoTipo'],['album','#filtroProdutoAlbum'],['ano','#filtroProdutoAno'],
    ['selecao','#filtroProdutoSelecao'],['categoria','#filtroProdutoCategoria'],
    ['estoque','#filtroProdutoEstoque'],['busca','#filtroProdutoBusca']
  ];
  map.forEach(([k,id])=>{const v=val(id);if(v)p.set(k,v)});
  return p;
}

async function carregarPaginaProdutos(page=0) {
  try {
    const dados=await api(`/produtos/pagina?${produtoQuery(page)}`);
    produtoPage.number=dados.number;
    produtoPage.totalPages=dados.totalPages;
    produtoPage.totalElements=dados.totalElements;
    produtoPage.content=dados.content||[];
    renderProdutosPaginados();
  } catch(e) { toast(e.message); }
}

window.sortProdutos = field => {
  if (produtoPage.sort===field) produtoPage.dir=produtoPage.dir==='asc'?'desc':'asc';
  else {produtoPage.sort=field;produtoPage.dir='asc'}
  carregarPaginaProdutos(0);
};

function thProduto(label,field) {
  const arrow=produtoPage.sort===field?(produtoPage.dir==='asc'?' ↑':' ↓'):' ⇅';
  return `<th class="sortable" onclick="sortProdutos('${field}')">${label}<span class="sort-indicator">${arrow}</span></th>`;
}

function precoCategoriaProduto(p){ const r=state.precos.find(x=>Number(x.album?.codigoAlbum)===Number(p.album?.codigoAlbum)&&Number(x.categoria?.codigoCategoria)===Number(p.categoria?.codigoCategoria)); return r?.valor??null; }
function tipoPrecoProduto(p){ if(p.precoEspecifico!=null)return 'Preço específico'; if(precoCategoriaProduto(p)!=null)return 'Por categoria'; return 'Sem preço'; }
function precoExibicaoProduto(p){ return p.precoEspecifico??precoCategoriaProduto(p)??0; }
window.definirEstoque=async(id,valor)=>{
  const estoque=Math.max(0,Math.trunc(Number(valor)||0));
  const pagina=produtoPage.content.find(x=>x.codigoProduto===id);
  const completo=state.produtos.find(x=>x.codigoProduto===id);
  const anterior=pagina?.estoque??completo?.estoque??0;
  const input=document.querySelector(`input[data-stock-id="${id}"]`);
  if(pagina) pagina.estoque=estoque;
  if(completo) completo.estoque=estoque;
  if(input) input.value=estoque;
  try{
    const atualizado=await api(`/produtos/${id}/estoque`,{method:'PATCH',body:JSON.stringify({estoque})});
    const confirmado=atualizado?.estoque??estoque;
    if(pagina) pagina.estoque=confirmado;
    if(completo) completo.estoque=confirmado;
    if(input) input.value=confirmado;
  }catch(e){
    if(pagina) pagina.estoque=anterior;
    if(completo) completo.estoque=anterior;
    if(input) input.value=anterior;
    toast(e.message);
  }
};
window.ajustarEstoque=(id,delta)=>{
  const input=document.querySelector(`input[data-stock-id="${id}"]`);
  const atual=input?Number(input.value):((produtoPage.content.find(x=>x.codigoProduto===id)||state.produtos.find(x=>x.codigoProduto===id))?.estoque||0);
  definirEstoque(id,Math.max(0,atual+delta));
};

function renderProdutosPaginados() {
  const rows=produtoPage.content;
  $('#listaProdutos').innerHTML=`<table><thead><tr>${thProduto('Número','numero')}${thProduto('Seleção','selecao')}${thProduto('Produto','produto')}${thProduto('Tipo','tipo')}${thProduto('Álbum','album')}${thProduto('Ano','ano')}${thProduto('Categoria','categoria')}<th>Tipo de preço</th>${thProduto('Preço','preco')}${thProduto('Estoque','estoque')}<th>Ações</th></tr></thead><tbody>${
    rows.map(p=>`<tr><td>${p.numeroFigurinha??'—'}</td><td>${esc(p.selecaoFigurinha||'—')}</td><td>${esc(p.nomeProduto||'—')}</td><td>${esc(tipoProdutoLabel(p))}</td><td>${esc(p.album?.nomeAlbum||'—')}</td><td>${p.album?.ano??'—'}</td><td>${esc(p.categoria?.nomeCategoria||'—')}</td><td>${esc(tipoPrecoProduto(p))}</td><td>${moeda(precoExibicaoProduto(p))}</td><td><div class="stock-control"><button type="button" onclick="ajustarEstoque(${p.codigoProduto},-1)">−</button><input type="number" min="0" data-stock-id="${p.codigoProduto}" value="${p.estoque}" onchange="definirEstoque(${p.codigoProduto},this.value)"><button type="button" onclick="ajustarEstoque(${p.codigoProduto},1)">+</button></div></td><td><button onclick="editarProduto(${p.codigoProduto})">Editar</button> <button class="danger" onclick="deletar('/produtos/${p.codigoProduto}')">Excluir</button></td></tr>`).join('')
  }</tbody></table>`;
  renderPagination('#paginacaoProdutos',produtoPage,p=>carregarPaginaProdutos(p));
}

function renderPagination(selector,pageState,go) {
  const box=$(selector);
  if (!box) return;
  if (pageState.totalPages<=1) {
    box.innerHTML=pageState.totalElements?`<span class="page-info">${pageState.totalElements} registro(s)</span>`:'';
    return;
  }
  const atual=pageState.number;
  const total=pageState.totalPages;
  const pages=new Set([0,total-1]);
  for(let i=Math.max(0,atual-2);i<=Math.min(total-1,atual+2);i++)pages.add(i);
  const ordered=[...pages].sort((a,b)=>a-b);
  let html=`<button ${atual===0?'disabled':''} data-page="${atual-1}">‹</button>`;
  let prev=-1;
  ordered.forEach(p=>{
    if(prev>=0&&p-prev>1)html+=`<span>…</span>`;
    html+=`<button class="${p===atual?'active-page':''}" data-page="${p}">${p+1}</button>`;
    prev=p;
  });
  html+=`<button ${atual===total-1?'disabled':''} data-page="${atual+1}">›</button><span class="page-info">${pageState.totalElements} registro(s)</span>`;
  box.innerHTML=html;
  box.querySelectorAll('button[data-page]:not([disabled])').forEach(b=>b.onclick=()=>go(Number(b.dataset.page)));
}

/* ---------- Regras do formulário de produto ---------- */
function albumExigeSelecao(codigoAlbum) {
  const a=state.albuns.find(x=>String(x.codigoAlbum)===String(codigoAlbum));
  return !!a && a.tipoAlbum==='COPA_DO_MUNDO' && Number(a.ano)>=2022;
}

function atualizarCamposProduto() {
  const tipo=val('#produtoTipo');
  const custom=tipo.startsWith('CUSTOM:');
  const figurinha=tipo==='FIGURINHA';
  const album=tipo==='ALBUM_COMPLETO'||tipo==='ALBUM_INCOMPLETO';
  const produtoAlbum=$('#produtoAlbum'),cat=$('#produtoCategoria'),sel=$('#produtoSelecao'),numero=$('#produtoNumero');

  produtoAlbum.disabled=false;
  cat.disabled=!figurinha;
  numero.disabled=!figurinha;

  if (!figurinha) {
    cat.value='';
    numero.value='';
  }

  const exige=figurinha && albumExigeSelecao(produtoAlbum.value);
  sel.disabled=!exige;
  if(!exige)sel.value='';

  const albumSelecionado=state.albuns.find(a=>String(a.codigoAlbum)===String(produtoAlbum.value));
  $('#produtoAno').value=albumSelecionado?.ano??'';
  if(album){
    cat.value='';sel.value='';numero.value='';
  }
}

$('#produtoTipo').addEventListener('change',atualizarCamposProduto);
$('#produtoAlbum').addEventListener('change',atualizarCamposProduto);
$('#produtoSelecao').addEventListener('input',e=>e.target.value=e.target.value.toUpperCase().replace(/[^A-Z]/g,'').slice(0,3));

$('#btnNovoProduto').onclick=()=>{
  $('#formProduto').classList.toggle('hidden');
  if(!$('#formProduto').classList.contains('hidden')){
    edit.produto=null;$('#formProduto').reset();$('#produtoTipo').disabled=false;refreshSelects();atualizarCamposProduto();
  }
};
$('#produtoCancelar').onclick=()=>{
  edit.produto=null;$('#formProduto').classList.remove('edit-modal-overlay');$('#produtoSubmit').textContent='Salvar';$('#formProduto').reset();$('#produtoTipo').disabled=false;$('#produtoCancelar').classList.add('hidden');$('#formProduto').classList.add('hidden');refreshSelects();
};

$('#formProduto').onsubmit=async e=>{
  e.preventDefault();
  const tipo=val('#produtoTipo');
  const custom=tipo.startsWith('CUSTOM:');
  const payload={
    tipoProduto:custom?null:tipo,
    codigoTipoCustomizado:custom?Number(tipo.split(':')[1]):null,
    codigoAlbum:$('#produtoAlbum').disabled?null:num('#produtoAlbum'),
    codigoCategoria:$('#produtoCategoria').disabled?null:num('#produtoCategoria'),
    selecaoFigurinha:$('#produtoSelecao').disabled?null:(val('#produtoSelecao')||null),
    numeroFigurinha:$('#produtoNumero').disabled?null:num('#produtoNumero'),
    nomeProduto:val('#produtoNome')||null,
    precoEspecifico:num('#produtoPreco'),
    estoque:num('#produtoEstoque')
  };
  try{
    await api(edit.produto?`/produtos/${edit.produto}`:'/produtos',{method:edit.produto?'PUT':'POST',body:JSON.stringify(payload)});
    edit.produto=null;e.target.reset();$('#formProduto').classList.remove('edit-modal-overlay');$('#produtoTipo').disabled=false;$('#produtoCancelar').classList.add('hidden');$('#formProduto').classList.add('hidden');
    await loadAll();
  }catch(err){toast(err.message)}
};

window.editarProduto=id=>{
  const p=state.produtos.find(x=>x.codigoProduto===id)||produtoPage.content.find(x=>x.codigoProduto===id);
  if(!p)return;
  edit.produto=id;$('#formProduto').classList.remove('hidden');$('#formProduto').classList.add('edit-modal-overlay');$('#produtoSubmit').textContent='Salvar';refreshSelects();
  $('#produtoTipo').value=produtoTipoValue(p);$('#produtoTipo').disabled=true;
  $('#produtoAlbum').value=p.album?.codigoAlbum||'';$('#produtoCategoria').value=p.categoria?.codigoCategoria||'';
  $('#produtoSelecao').value=p.selecaoFigurinha||'';$('#produtoNumero').value=p.numeroFigurinha??'';
  $('#produtoNome').value=p.nomeProduto||'';$('#produtoPreco').value=p.precoEspecifico??'';$('#produtoEstoque').value=p.estoque;
  atualizarCamposProduto();$('#produtoCancelar').classList.remove('hidden');
};

/* ---------- Tipos personalizados ---------- */
$('#btnTiposProduto').onclick=()=>{$('#tiposProdutoModal').classList.remove('hidden');renderTiposProduto()};
$('#fecharTiposProduto').onclick=()=>$('#tiposProdutoModal').classList.add('hidden');
$('#tipoProdutoCancelar').onclick=()=>{edit.tipo=null;$('#formTipoProduto').reset();$('#tipoProdutoCancelar').classList.add('hidden')};

function renderTiposProduto(){
  $('#listaTiposProduto').innerHTML=`<table><thead><tr><th>Tipo</th><th>Origem</th><th>Ações</th></tr></thead><tbody>
    <tr><td>Figurinha</td><td>Sistema</td><td>—</td></tr>
    <tr><td>Álbum completo</td><td>Sistema</td><td>—</td></tr>
    <tr><td>Álbum incompleto</td><td>Sistema</td><td>—</td></tr>
    ${state.tiposCustomizados.map(t=>`<tr><td>${esc(t.nome)}</td><td>Personalizado</td><td><button onclick="editarTipoProduto(${t.codigoTipoProduto})">Editar</button> <button class="danger" onclick="deletar('/tipos-produto/${t.codigoTipoProduto}')">Excluir</button></td></tr>`).join('')}
  </tbody></table>`;
}
window.editarTipoProduto=id=>{const t=state.tiposCustomizados.find(x=>x.codigoTipoProduto===id);edit.tipo=id;$('#tipoProdutoNome').value=t.nome;$('#tipoProdutoCancelar').classList.remove('hidden')};
$('#formTipoProduto').onsubmit=async e=>{e.preventDefault();try{await api(edit.tipo?`/tipos-produto/${edit.tipo}`:'/tipos-produto',{method:edit.tipo?'PUT':'POST',body:JSON.stringify({nome:val('#tipoProdutoNome')})});edit.tipo=null;e.target.reset();$('#tipoProdutoCancelar').classList.add('hidden');await loadAll();$('#tiposProdutoModal').classList.remove('hidden')}catch(err){toast(err.message)}};

/* ---------- CRUDs simples ---------- */
window.deletar=async url=>{if(!confirm('Excluir este registro?'))return;try{await api(url,{method:'DELETE'});await loadAll();toast('Excluído com sucesso.')}catch(e){toast(e.message)}};
window.postAction=async url=>{try{await api(url,{method:'POST'});await loadAll()}catch(e){toast(e.message)}};

window.editarAlbum=id=>{const a=state.albuns.find(x=>x.codigoAlbum===id);edit.album=id;$('#formAlbum').classList.add('edit-modal-overlay');$('#albumSubmit').textContent='Salvar';$('#albumNome').value=a.nomeAlbum;$('#albumAno').value=a.ano??'';$('#albumTipo').value=a.tipoAlbum||'OUTRO';$('#albumCancelar').classList.remove('hidden')};
$('#albumCancelar').onclick=()=>{edit.album=null;$('#formAlbum').reset();$('#formAlbum').classList.remove('edit-modal-overlay');$('#albumSubmit').textContent='Cadastrar';$('#albumCancelar').classList.add('hidden')};
$('#formAlbum').onsubmit=async e=>{e.preventDefault();try{await api(edit.album?`/albuns/${edit.album}`:'/albuns',{method:edit.album?'PUT':'POST',body:JSON.stringify({nomeAlbum:val('#albumNome'),ano:num('#albumAno'),tipoAlbum:val('#albumTipo')})});edit.album=null;e.target.reset();$('#formAlbum').classList.remove('edit-modal-overlay');$('#albumSubmit').textContent='Cadastrar';$('#albumCancelar').classList.add('hidden');await loadAll()}catch(err){toast(err.message)}};

window.editarCategoria=id=>{const c=state.categorias.find(x=>x.codigoCategoria===id);edit.categoria=id;$('#categoriaNome').value=c.nomeCategoria;$('#categoriaCancelar').classList.remove('hidden')};
$('#categoriaCancelar').onclick=()=>{edit.categoria=null;$('#formCategoria').reset();$('#categoriaCancelar').classList.add('hidden')};
$('#formCategoria').onsubmit=async e=>{e.preventDefault();try{await api(edit.categoria?`/categorias/${edit.categoria}`:'/categorias',{method:edit.categoria?'PUT':'POST',body:JSON.stringify({nomeCategoria:val('#categoriaNome')})});edit.categoria=null;e.target.reset();$('#categoriaCancelar').classList.add('hidden');await loadAll()}catch(err){toast(err.message)}};

window.editarCliente=id=>{const c=state.clientes.find(x=>x.codigoCliente===id);edit.cliente=id;$('#clienteNome').value=c.nomeCliente;$('#clienteEmail').value=c.emailCliente||'';$('#clienteTelefone').value=c.telefoneCliente||'';$('#clienteCancelar').classList.remove('hidden')};
$('#clienteCancelar').onclick=()=>{edit.cliente=null;$('#formCliente').reset();$('#clienteCancelar').classList.add('hidden')};
$('#formCliente').onsubmit=async e=>{e.preventDefault();try{await api(edit.cliente?`/clientes/${edit.cliente}`:'/clientes',{method:edit.cliente?'PUT':'POST',body:JSON.stringify({nomeCliente:val('#clienteNome'),emailCliente:val('#clienteEmail')||null,telefoneCliente:val('#clienteTelefone')||null})});edit.cliente=null;e.target.reset();$('#clienteCancelar').classList.add('hidden');await loadAll()}catch(err){toast(err.message)}};

window.editarPreco=id=>{const p=state.precos.find(x=>x.codigoPreco===id);edit.preco=id;refreshSelects();$('#precoAlbum').value=p.album?.codigoAlbum||'';$('#precoCategoria').value=p.categoria?.codigoCategoria||'';$('#precoValor').value=p.valor;$('#precoAlbum').disabled=true;$('#precoCategoria').disabled=true;$('#precoCancelar').classList.remove('hidden')};
$('#precoCancelar').onclick=()=>{edit.preco=null;$('#formPreco').reset();$('#precoAlbum').disabled=false;$('#precoCategoria').disabled=false;$('#precoCancelar').classList.add('hidden');refreshSelects()};
$('#formPreco').onsubmit=async e=>{e.preventDefault();try{await api(edit.preco?`/precos-categoria-album/${edit.preco}`:'/precos-categoria-album',{method:edit.preco?'PUT':'POST',body:JSON.stringify({codigoAlbum:num('#precoAlbum'),codigoCategoria:num('#precoCategoria'),valor:num('#precoValor')})});edit.preco=null;e.target.reset();$('#precoAlbum').disabled=false;$('#precoCategoria').disabled=false;$('#precoCancelar').classList.add('hidden');await loadAll()}catch(err){toast(err.message)}};

/* ---------- Compras ---------- */
window.editarCompra=id=>{const c=state.compras.find(x=>x.codigoCompra===id);edit.compra=id;$('#compraData').value=c.data||'';$('#compraDescricao').value=c.descricao;$('#compraValor').value=c.valorTotal;$('#compraObservacao').value=c.observacao||'';$('#compraCancelar').classList.remove('hidden')};
$('#compraCancelar').onclick=()=>{edit.compra=null;$('#formCompra').reset();$('#compraCancelar').classList.add('hidden')};
$('#formCompra').onsubmit=async e=>{e.preventDefault();try{const p={data:val('#compraData')||null,descricao:val('#compraDescricao'),valorTotal:num('#compraValor'),observacao:val('#compraObservacao')||null};await api(edit.compra?`/compras-produtos/${edit.compra}`:'/compras-produtos',{method:edit.compra?'PUT':'POST',body:JSON.stringify(p)});edit.compra=null;e.target.reset();$('#compraCancelar').classList.add('hidden');await loadAll()}catch(err){toast(err.message)}};

/* ---------- Configuração Shopee ---------- */
function atualizarCamposShopee(){
  const shopee=val('#configShopee')==='true';
  $$('.shopee-field').forEach(e=>e.classList.toggle('hidden',!shopee));
  $$('.manual-tax-field').forEach(e=>e.classList.toggle('hidden',shopee));
  $('.shopee-info')?.classList.toggle('hidden',!shopee);

  const cnpj=shopee&&val('#configTipoVendedor')==='CNPJ';
  $('#config450').disabled=cnpj||!shopee;
  if(cnpj||!shopee)$('#config450').value='false';
}
$('#configShopee').addEventListener('change',atualizarCamposShopee);
$('#configTipoVendedor').addEventListener('change',atualizarCamposShopee);

window.editarConfiguracao=id=>{
  const c=state.configuracoes.find(x=>x.codigoConfiguracao===id);edit.config=id;
  $('#configNome').value=c.nome;$('#configShopee').value=String(c.configuracaoShopee);
  $('#configTipoVendedor').value=c.tipoVendedorShopee||'';$('#config450').value=String(c.cpfAcima450Pedidos);
  $('#configEmbalagem').value=c.embalagem??0;$('#configTaxaPercentual').value=c.taxaPercentual??0;$('#configTaxaFixa').value=c.taxaFixa??0;
  $('#configCancelar').classList.remove('hidden');atualizarCamposShopee();
};
$('#configCancelar').onclick=()=>{edit.config=null;$('#formConfiguracao').reset();$('#configCancelar').classList.add('hidden');atualizarCamposShopee()};
$('#formConfiguracao').onsubmit=async e=>{
  e.preventDefault();
  const shopee=val('#configShopee')==='true';
  const vendedor=shopee?val('#configTipoVendedor'):null;
  const p={
    nome:val('#configNome'),embalagem:Number(val('#configEmbalagem')||0),
    taxaPercentual:shopee?0:Number(val('#configTaxaPercentual')||0),
    taxaFixa:shopee?0:Number(val('#configTaxaFixa')||0),
    configuracaoShopee:shopee,tipoVendedorShopee:vendedor||null,
    cpfAcima450Pedidos:shopee&&vendedor==='CPF'&&val('#config450')==='true'
  };
  try{await api(edit.config?`/configuracoes-venda/${edit.config}`:'/configuracoes-venda',{method:edit.config?'PUT':'POST',body:JSON.stringify(p)});edit.config=null;e.target.reset();$('#configCancelar').classList.add('hidden');atualizarCamposShopee();await loadAll()}catch(err){toast(err.message)}
};

$('#formRegraShopee').onsubmit=async e=>{e.preventDefault();try{await api('/regras-shopee',{method:'POST',body:JSON.stringify({valorItem:num('#regraValor'),taxaTotal:num('#regraTaxa')})});e.target.reset();await loadAll()}catch(err){toast(err.message)}};

/* ---------- Picker compacto de itens ---------- */
function buildPicker(contexto) {
  const container = $(`#${contexto}Picker`);
  if (!container) return;
  container.innerHTML=`
    <label>Tipo de produto<select class="picker-tipo"></select></label>
    <label class="picker-album-tipo-wrap">Tipo do álbum<select class="picker-album-tipo"></select></label>
    <label class="picker-ano-wrap">Ano<select class="picker-ano"></select></label>
    <label class="picker-selecao-wrap">Seleção<select class="picker-selecao"></select></label>
    <label class="picker-numero-wrap">Número<select class="picker-numero"></select></label>
    <label class="picker-produto-wrap">Produto<select class="picker-produto"></select></label>
    <label>Quantidade<input class="picker-qtd" type="number" min="1" value="1"></label>
    <div class="picker-action"><button type="button" class="picker-add">Adicionar</button></div>
  `;

  const tipoSel=container.querySelector('.picker-tipo');
  tipoSel.innerHTML=`<option value="">Selecione</option>`+tiposProdutoOptions().map(o=>`<option value="${esc(o.value)}">${esc(o.label)}</option>`).join('');

  const atualizar=()=>atualizarPicker(contexto);
  ['.picker-tipo','.picker-album-tipo','.picker-ano','.picker-selecao'].forEach(s=>container.querySelector(s).addEventListener('change',atualizar));
  container.querySelector('.picker-add').onclick=()=>adicionarDoPicker(contexto);
  atualizarPicker(contexto);
}

function pickerProdutosBase(contexto) {
  const container=$(`#${contexto}Picker`);
  const tipo=container.querySelector('.picker-tipo').value;
  if(!tipo)return [];
  if(tipo.startsWith('CUSTOM:')){
    const id=Number(tipo.split(':')[1]);
    return state.produtos.filter(p=>p.tipoProdutoCustomizado?.codigoTipoProduto===id);
  }
  return state.produtos.filter(p=>p.tipoProduto===tipo);
}

function atualizarPicker(contexto) {
  const c=$(`#${contexto}Picker`); if(!c)return;
  const tipo=c.querySelector('.picker-tipo').value;
  const isCustom=tipo.startsWith('CUSTOM:');
  const figurinha=tipo==='FIGURINHA';
  const albumProduto=tipo==='ALBUM_COMPLETO'||tipo==='ALBUM_INCOMPLETO';

  const wrapTipo=c.querySelector('.picker-album-tipo-wrap'),wrapAno=c.querySelector('.picker-ano-wrap');
  const wrapSel=c.querySelector('.picker-selecao-wrap'),wrapNum=c.querySelector('.picker-numero-wrap');
  const wrapProd=c.querySelector('.picker-produto-wrap');

  wrapTipo.classList.toggle('hidden',isCustom||!tipo);
  wrapAno.classList.toggle('hidden',isCustom||!tipo);
  wrapNum.classList.toggle('hidden',!figurinha);
  wrapProd.classList.toggle('hidden',figurinha||!tipo);

  let produtos=pickerProdutosBase(contexto);
  const albumTipos=uniqueSorted(produtos.map(p=>p.album?.tipoAlbum));
  const tipoAlbumSel=c.querySelector('.picker-album-tipo');
  const oldTipo=tipoAlbumSel.value;
  tipoAlbumSel.innerHTML='<option value="">Selecione</option>'+albumTipos.map(t=>`<option value="${esc(t)}">${esc(tipoAlbumLabel(t))}</option>`).join('');
  if(albumTipos.includes(oldTipo))tipoAlbumSel.value=oldTipo;
  if(!tipoAlbumSel.value&&albumTipos.length===1)tipoAlbumSel.value=albumTipos[0];

  const tipoAlbum=tipoAlbumSel.value;
  if(tipoAlbum)produtos=produtos.filter(p=>p.album?.tipoAlbum===tipoAlbum);

  const anos=uniqueSorted(produtos.map(p=>p.album?.ano),true);
  const anoSel=c.querySelector('.picker-ano');
  const oldAno=anoSel.value;
  anoSel.innerHTML='<option value="">Selecione</option>'+anos.map(a=>`<option value="${a}">${a}</option>`).join('');
  if(anos.map(String).includes(oldAno))anoSel.value=oldAno;
  if(!anoSel.value&&anos.length===1)anoSel.value=String(anos[0]);

  const ano=anoSel.value;
  if(ano)produtos=produtos.filter(p=>String(p.album?.ano)===ano);

  const exigeSelecao=figurinha&&tipoAlbum==='COPA_DO_MUNDO'&&Number(ano)>=2022;
  wrapSel.classList.toggle('hidden',!exigeSelecao);
  const selSel=c.querySelector('.picker-selecao');
  if(exigeSelecao){
    const sels=uniqueSorted(produtos.map(p=>p.selecaoFigurinha));
    const old=selSel.value;
    selSel.innerHTML='<option value="">Selecione</option>'+sels.map(s=>`<option value="${esc(s)}">${esc(s)}</option>`).join('');
    if(sels.includes(old))selSel.value=old;
    if(selSel.value)produtos=produtos.filter(p=>p.selecaoFigurinha===selSel.value);
  }else{
    selSel.innerHTML='<option value="">Não se aplica</option>';
  }

  if(figurinha){
    const numeroSel=c.querySelector('.picker-numero');
    const old=numeroSel.value;
    const figs=[...produtos].sort((a,b)=>(a.numeroFigurinha||0)-(b.numeroFigurinha||0));
    numeroSel.innerHTML='<option value="">Selecione</option>'+figs.map(p=>`<option value="${p.codigoProduto}">${p.numeroFigurinha}</option>`).join('');
    if(figs.some(p=>String(p.codigoProduto)===old))numeroSel.value=old;
  }else{
    const produtoSel=c.querySelector('.picker-produto');
    const old=produtoSel.value;
    produtoSel.innerHTML='<option value="">Selecione</option>'+produtos.map(p=>`<option value="${p.codigoProduto}">${esc(itemIdentificacao(p))}</option>`).join('');
    if(produtos.some(p=>String(p.codigoProduto)===old))produtoSel.value=old;
  }
}

function adicionarDoPicker(contexto) {
  const c=$(`#${contexto}Picker`);
  const tipo=c.querySelector('.picker-tipo').value;
  const figurinha=tipo==='FIGURINHA';
  const codigo=Number(figurinha?c.querySelector('.picker-numero').value:c.querySelector('.picker-produto').value);
  const qtd=Number(c.querySelector('.picker-qtd').value);
  if(!codigo||!qtd||qtd<1)return toast('Selecione o produto e a quantidade.');
  const produto=state.produtos.find(p=>p.codigoProduto===codigo);
  adicionarItemCompacto(contexto,produto,qtd);
}

function itensArray(contexto) {
  return contexto==='venda'?vendaItensSelecionados:orcamentoItensSelecionados;
}

function adicionarItemCompacto(contexto,produto,quantidade=1) {
  if(!produto)return;
  const arr=itensArray(contexto);
  const existente=arr.find(i=>i.produto.codigoProduto===produto.codigoProduto);
  if(existente)existente.quantidade+=quantidade;
  else arr.push({produto,quantidade});
  renderItensCompactos(contexto);
}

function renderItensCompactos(contexto) {
  const arr=itensArray(contexto), size=10, total=Math.max(1,Math.ceil(arr.length/size)); itemPages[contexto]=Math.min(itemPages[contexto],total-1);
  const ini=itemPages[contexto]*size, page=arr.slice(ini,ini+size);
  const body=$(`#${contexto}ItensBody`);
  body.innerHTML=page.map((i,local)=>{const idx=ini+local;return `<tr><td>${esc(itemIdentificacao(i.produto))}</td><td>${esc(tipoProdutoLabel(i.produto))}</td><td>${esc(i.produto.album?albumDescricao(i.produto.album):'—')}</td><td><input type="number" min="1" value="${i.quantidade}" onchange="alterarQtdItem('${contexto}',${idx},this.value)"></td><td><button type="button" class="danger" onclick="removerItem('${contexto}',${idx})">Remover</button></td></tr>`}).join('');
  const box=$(`#${contexto}ItensPaginacao`); if(box)box.innerHTML=arr.length>10?`<button type="button" ${itemPages[contexto]===0?'disabled':''} onclick="mudarPaginaItens('${contexto}',-1)">‹</button><span>${itemPages[contexto]+1}/${total}</span><button type="button" ${itemPages[contexto]>=total-1?'disabled':''} onclick="mudarPaginaItens('${contexto}',1)">›</button>`:'';
}
window.mudarPaginaItens=(c,d)=>{itemPages[c]=Math.max(0,itemPages[c]+d);renderItensCompactos(c)};

window.alterarQtdItem=(contexto,idx,valor)=>{const arr=itensArray(contexto);arr[idx].quantidade=Math.max(1,Number(valor)||1);renderItensCompactos(contexto)};
window.removerItem=(contexto,idx)=>{itensArray(contexto).splice(idx,1);renderItensCompactos(contexto)};

/* ---------- Lista colada ---------- */
function produtoFigurinhaDoAlbum(codigoAlbum,selecao,numero){
  return state.produtos.find(p =>
    p.tipoProduto==='FIGURINHA' &&
    Number(p.album?.codigoAlbum)===Number(codigoAlbum) &&
    Number(p.numeroFigurinha)===Number(numero) &&
    ((selecao??null)===(p.selecaoFigurinha??null))
  );
}

function interpretarTextoFigurinhas(codigoAlbum,texto){
  const album=state.albuns.find(a=>Number(a.codigoAlbum)===Number(codigoAlbum));
  if(!album)throw new Error('Selecione o álbum da lista.');
  const copaComSelecao=album.tipoAlbum==='COPA_DO_MUNDO'&&Number(album.ano)>=2022;
  const achados=new Map(),resultados=[];
  const linhas=String(texto||'').split(/\r?\n/).map(l=>l.trim()).filter(Boolean);
  if(!linhas.length)throw new Error('Cole uma lista de figurinhas.');

  const registrar=(original,selecao,numero)=>{
    const produto=produtoFigurinhaDoAlbum(codigoAlbum,selecao,numero);
    const codigo=selecao ? `${selecao}${numero}` : (numero===0?'00':String(numero));
    resultados.push({entrada:original,normalizado:codigo,produto,status:produto?'Encontrada':'Não cadastrada'});
    if(produto){
      const k=produto.codigoProduto;
      achados.set(k,{produto,quantidade:(achados.get(k)?.quantidade||0)+1});
    }
  };

  for(const original of linhas){
    const linha=original.toUpperCase().trim();
    if(!copaComSelecao){
      const numeros=(linha.match(/\d+/g)||[]).map(Number);
      if(!numeros.length){resultados.push({entrada:original,normalizado:'—',produto:null,status:'Nenhum número encontrado'});continue}
      numeros.forEach(numero=>registrar(original,null,numero));
      continue;
    }

    // Copa 2022+: aceita linhas como BRA17, "BRA 17", "BRA: 3,4",
    // vários grupos na mesma linha, FWC/CC e a figurinha especial 00.
    let encontrouGrupo=false;
    const re=/([A-Z]{2,3})\s*[-:]?\s*((?:\d+\s*[,;\s-]*)+)/g;
    let m;
    while((m=re.exec(linha))!==null){
      encontrouGrupo=true;
      const selecao=m[1];
      const numeros=(m[2].match(/\d+/g)||[]).map(Number);
      numeros.forEach(numero=>registrar(original,selecao,numero));
    }
    if(encontrouGrupo)continue;

    // O item 00 do catálogo não possui prefixo de seleção.
    const somenteNumeros=(linha.match(/\d+/g)||[]).map(Number);
    if(somenteNumeros.length && somenteNumeros.every(n=>n===0)){
      somenteNumeros.forEach(numero=>registrar(original,null,numero));
      continue;
    }
    resultados.push({entrada:original,normalizado:'—',produto:null,status:'Formato inválido'});
  }
  return {resultados,achados:[...achados.values()]};
}
function renderPreviewLista(contexto,interpretacao){
  listasInterpretadas[contexto]=interpretacao.achados; previewResultados[contexto]=interpretacao.resultados; previewPages[contexto]=0; renderPreviewPagina(contexto);
}
function renderPreviewPagina(contexto){
  const resultados=previewResultados[contexto];
  const size=10;
  const total=Math.max(1,Math.ceil(resultados.length/size));
  previewPages[contexto]=Math.max(0,Math.min(previewPages[contexto],total-1));

  const inicio=previewPages[contexto]*size;
  const page=resultados.slice(inicio,inicio+size);
  const preview=$(contexto==='orcamento'?'#orcListaPreview':'#vendaListaPreview');
  const botao=$(contexto==='orcamento'?'#adicionarOrcLista':'#adicionarVendaLista');

  const paginacao=resultados.length>size
    ? `<div class="pagination"><button type="button" class="preview-anterior" ${previewPages[contexto]===0?'disabled':''}>‹</button><span>${previewPages[contexto]+1}/${total}</span><button type="button" class="preview-proxima" ${previewPages[contexto]>=total-1?'disabled':''}>›</button></div>`
    : '';

  preview.innerHTML=`<table><thead><tr><th>Figurinha</th><th>Status</th></tr></thead><tbody>${page.map(r=>`<tr><td>${esc(r.normalizado)}</td><td class="${r.produto?'lista-ok':'lista-erro'}">${r.produto?'✓ Encontrada':'✕ '+esc(r.status)}</td></tr>`).join('')}</tbody></table>${paginacao}`;

  const anterior=preview.querySelector('.preview-anterior');
  const proxima=preview.querySelector('.preview-proxima');
  if(anterior) anterior.addEventListener('click',()=>mudarPreviewPagina(contexto,-1));
  if(proxima) proxima.addEventListener('click',()=>mudarPreviewPagina(contexto,1));

  const totalEncontradas=resultados.filter(r=>r.produto).length;
  botao.classList.toggle('hidden',totalEncontradas===0);
}
function mudarPreviewPagina(contexto,delta){
  const total=Math.max(1,Math.ceil(previewResultados[contexto].length/10));
  previewPages[contexto]=Math.max(0,Math.min(previewPages[contexto]+delta,total-1));
  renderPreviewPagina(contexto);
}

function adicionarTodasEncontradas(contexto){
  const encontradas=listasInterpretadas[contexto]||[];
  if(!encontradas.length)return toast('Nenhuma figurinha encontrada para adicionar.');

  let unidades=0;
  encontradas.forEach(i=>{
    const quantidade=Math.max(1,Number(i.quantidade)||1);
    unidades+=quantidade;
    const arr=itensArray(contexto);
    const existente=arr.find(x=>x.produto.codigoProduto===i.produto.codigoProduto);
    if(existente) existente.quantidade+=quantidade;
    else arr.push({produto:i.produto,quantidade});
  });

  itemPages[contexto]=0;
  renderItensCompactos(contexto);
  const botao=$(contexto==='orcamento'?'#adicionarOrcLista':'#adicionarVendaLista');
  botao.classList.add('hidden');
  toast(`${encontradas.length} figurinha(s) diferente(s) adicionada(s) — ${unidades} unidade(s) no total.`);
}
$('#interpretarVendaLista').onclick=()=>{try{renderPreviewLista('venda',interpretarTextoFigurinhas(num('#vendaListaAlbum'),val('#vendaListaTexto')))}catch(e){toast(e.message)}};
$('#interpretarOrcLista').onclick=()=>{try{renderPreviewLista('orcamento',interpretarTextoFigurinhas(num('#orcListaAlbum'),val('#orcListaTexto')))}catch(e){toast(e.message)}};
$('#adicionarVendaLista').onclick=()=>adicionarTodasEncontradas('venda');
$('#adicionarOrcLista').onclick=()=>adicionarTodasEncontradas('orcamento');

/* ---------- Venda e orçamento ---------- */
function payloadItens(contexto){
  return itensArray(contexto).map(i=>({codigoProduto:i.produto.codigoProduto,quantidade:i.quantidade}));
}

$('#vendaFretePago').onchange=()=>$('#vendaFreteWrap').classList.toggle('hidden',val('#vendaFretePago')!=='true');
$('#vendaConfiguracao').onchange=()=>{
  const c=state.configuracoes.find(x=>String(x.codigoConfiguracao)===val('#vendaConfiguracao'));
  if(!c){$('#vendaConfigResumo').innerHTML='Selecione uma configuração.';return}
  $('#vendaConfigResumo').innerHTML=c.configuracaoShopee
    ? `<b>Shopee:</b> taxas calculadas automaticamente sobre o valor total da venda. Embalagem: ${moeda(c.embalagem)}.`
    : `<b>${esc(c.nome)}:</b> taxa percentual ${Number(c.taxaPercentual||0)*100}% + taxa fixa ${moeda(c.taxaFixa)} + embalagem ${moeda(c.embalagem)}.`;
};

$('#formVenda').onsubmit=async e=>{
  e.preventDefault();
  const itens=payloadItens('venda');
  if(!itens.length)return toast('Adicione pelo menos um item.');
  const fretePago=val('#vendaFretePago')==='true';
  try{
    await api('/vendas',{method:'POST',body:JSON.stringify({
      codigoCliente:num('#vendaCliente'),codigoConfiguracao:num('#vendaConfiguracao'),
      fretePagoVendedor:fretePago,frete:fretePago?Number(val('#vendaFrete')||0):0,itens
    })});
    e.target.reset();vendaItensSelecionados.length=0;renderItensCompactos('venda');$('#vendaFreteWrap').classList.add('hidden');
    await loadAll();
  }catch(err){toast(err.message)}
};

$('#formOrcamento').onsubmit=async e=>{
  e.preventDefault();
  const itens=payloadItens('orcamento');
  if(!itens.length)return toast('Adicione pelo menos um item.');
  try{
    await api('/orcamentos',{method:'POST',body:JSON.stringify({codigoCliente:num('#orcamentoCliente'),itens})});
    e.target.reset();orcamentoItensSelecionados.length=0;renderItensCompactos('orcamento');await loadAll();
  }catch(err){toast(err.message)}
};

/* ---------- Conversão de orçamento ---------- */
window.abrirConverterOrcamento=id=>{
  $('#converterOrcamentoId').value=id;refreshSelects();$('#converterOrcamentoModal').classList.remove('hidden');
};
$('#fecharConverterOrcamento').onclick=()=>$('#converterOrcamentoModal').classList.add('hidden');
$('#converterFretePago').onchange=()=>$('#converterFreteWrap').classList.toggle('hidden',val('#converterFretePago')!=='true');
$('#formConverterOrcamento').onsubmit=async e=>{
  e.preventDefault();
  const id=Number($('#converterOrcamentoId').value),pago=val('#converterFretePago')==='true';
  try{
    await api(`/orcamentos/${id}/converter`,{method:'POST',body:JSON.stringify({codigoConfiguracao:num('#converterConfiguracao'),fretePagoVendedor:pago,frete:pago?Number(val('#converterFrete')||0):0})});
    $('#converterOrcamentoModal').classList.add('hidden');e.target.reset();await loadAll();toast('Orçamento convertido em venda.');
  }catch(err){toast(err.message)}
};

/* ---------- Histórico Pageable ---------- */
function historicoQuery(page=historicoPage.number){
  const p=new URLSearchParams({page:String(page),size:'20',ordenarPor:historicoPage.sort,direcao:historicoPage.dir});
  const map=[
    ['dataInicio','#historicoDataInicio'],['dataFim','#historicoDataFim'],['status','#historicoStatus'],
    ['cliente','#historicoCliente'],['canal','#historicoCanal'],['tipoProduto','#historicoTipoProduto']
  ];
  map.forEach(([k,id])=>{const v=val(id);if(v)p.set(k,v)});
  return p;
}

async function carregarHistorico(page=0){
  try{
    const dados=await api(`/vendas/historico?${historicoQuery(page)}`);
    historicoPage.number=dados.number;historicoPage.totalPages=dados.totalPages;historicoPage.totalElements=dados.totalElements;
    historicoPage.content=dados.content||[];
    await Promise.all(historicoPage.content.map(async v=>{try{v._resumo=await api(`/vendas/${v.codigoVenda}/resumo`)}catch{}}));
    renderHistorico();
  }catch(e){toast(e.message)}
}

window.sortHistorico=field=>{
  if(historicoPage.sort===field)historicoPage.dir=historicoPage.dir==='asc'?'desc':'asc';
  else{historicoPage.sort=field;historicoPage.dir=field==='data'?'desc':'asc'}
  carregarHistorico(0);
};

function thHistorico(label,field){
  const arrow=historicoPage.sort===field?(historicoPage.dir==='asc'?' ↑':' ↓'):' ⇅';
  return `<th class="sortable" onclick="sortHistorico('${field}')">${label}<span class="sort-indicator">${arrow}</span></th>`;
}

function renderHistorico(){
  $('#listaHistoricoVendas').innerHTML=`<table><thead><tr>${thHistorico('Data','data')}${thHistorico('Cliente','cliente')}${thHistorico('Canal','canal')}${thHistorico('Status','status')}<th>Faturamento</th><th>Despesas</th><th>Resultado antes do estoque</th><th>Ações</th></tr></thead><tbody>${
    historicoPage.content.map(v=>`<tr><td>${esc(v.data||'—')}</td><td>${esc(v.cliente?.nomeCliente||'Sem cliente')}</td><td>${esc(v.configuracaoVenda?.nome||'—')}</td><td><span class="badge">${esc(v.statusVenda)}</span></td><td>${moeda(v._resumo?.faturamento)}</td><td>${moeda(v._resumo?.despesasTotais)}</td><td>${moeda(v._resumo?.lucro)}</td><td><button onclick="verVenda(${v.codigoVenda})">Detalhes</button></td></tr>`).join('')
  }</tbody></table>`;
  renderPagination('#paginacaoHistorico',historicoPage,p=>carregarHistorico(p));
}

/* ---------- Produtos vendidos ---------- */
function produtosVendidosQuery(){
  const p=new URLSearchParams();
  const map=[
    ['dataInicio','#produtosVendidosDataInicio'],['dataFim','#produtosVendidosDataFim'],
    ['tipo','#produtosVendidosTipo'],['album','#produtosVendidosAlbum'],['ano','#produtosVendidosAno'],
    ['selecao','#produtosVendidosSelecao'],['numero','#produtosVendidosNumero']
  ];
  map.forEach(([k,id])=>{const v=val(id);if(v)p.set(k,v)});
  return p;
}

async function carregarProdutosVendidos(){
  try{
    const rows=await api(`/vendas/produtos-vendidos?${produtosVendidosQuery()}`);
    $('#listaProdutosVendidos').innerHTML=`<table><thead><tr><th>Produto</th><th>Tipo</th><th>Álbum</th><th>Ano</th><th>Seleção</th><th>Número</th><th>Quantidade vendida</th></tr></thead><tbody>${
      rows.map(r=>{const p=r.produto;return `<tr><td>${esc(p.nomeProduto||'—')}</td><td>${esc(tipoProdutoLabel(p))}</td><td>${esc(p.album?.nomeAlbum||'—')}</td><td>${p.album?.ano??'—'}</td><td>${esc(p.selecaoFigurinha||'—')}</td><td>${p.numeroFigurinha??'—'}</td><td>${r.quantidadeVendida}</td></tr>`}).join('')
    }</tbody></table>`;
    if(!rows.length)$('#listaProdutosVendidos').innerHTML='<div class="empty">Nenhum produto vendido para os filtros selecionados.</div>';
  }catch(e){toast(e.message)}
}

['#produtosVendidosDataInicio','#produtosVendidosDataFim','#produtosVendidosTipo','#produtosVendidosAlbum','#produtosVendidosAno','#produtosVendidosSelecao','#produtosVendidosNumero'].forEach(id=>{
  $(id)?.addEventListener('change',()=>{updateFilterButton($('#filtrosProdutosVendidos'));carregarProdutosVendidos()});
});
$('#limparFiltrosProdutosVendidos').onclick=()=>{
  $('#filtrosProdutosVendidos').querySelectorAll('input,select').forEach(el=>{el.value='';if(el.tagName==='SELECT')syncSearchableSelect(el)});
  updateFilterButton($('#filtrosProdutosVendidos'));carregarProdutosVendidos();
};

/* ---------- Detalhes compactos ---------- */
function gruposItensHtml(itens){
  const fig=new Map(),alb=new Map(),outros=[];
  itens.forEach(i=>{
    const p=i.produto;
    if(p.tipoProduto==='FIGURINHA'){
      const key=`${p.album?.nomeAlbum||tipoAlbumLabel(p.album?.tipoAlbum)} ${p.album?.ano||''}`.trim();
      const token=`${p.selecaoFigurinha||''}${p.numeroFigurinha??''}${i.quantidade>1?` ×${i.quantidade}`:''}`;
      if(!fig.has(key))fig.set(key,[]);fig.get(key).push(token);
    }else if(p.tipoProduto==='ALBUM_COMPLETO'||p.tipoProduto==='ALBUM_INCOMPLETO'){
      const key=albumDescricao(p.album);
      const label=`${p.tipoProduto==='ALBUM_COMPLETO'?'Álbum completo':'Álbum incompleto'}${i.quantidade>1?` ×${i.quantidade}`:''}`;
      if(!alb.has(key))alb.set(key,[]);alb.get(key).push(label);
    }else{
      outros.push(`${itemIdentificacao(p)}${i.quantidade>1?` ×${i.quantidade}`:''}`);
    }
  });

  let html='';
  if(fig.size){
    html+='<h3>Figurinhas</h3>';
    for(const [grupo,tokens] of fig)html+=`<div class="group-block"><h4>${esc(grupo)}</h4><div class="token-list">${tokens.map(t=>`<span class="item-token">${esc(t)}</span>`).join('')}</div></div>`;
  }
  if(alb.size){
    html+='<h3>Álbuns</h3>';
    for(const [grupo,tokens] of alb)html+=`<div class="group-block"><h4>${esc(grupo)}</h4><div class="token-list">${tokens.map(t=>`<span class="item-token">${esc(t)}</span>`).join('')}</div></div>`;
  }
  if(outros.length){
    html+='<h3>Outros produtos</h3><div class="group-block"><div class="token-list">'+outros.map(t=>`<span class="item-token">${esc(t)}</span>`).join('')+'</div></div>';
  }
  return html||'<div class="empty">Sem itens.</div>';
}

function modal(titulo,html){$('#modalTitulo').textContent=titulo;$('#modalConteudo').innerHTML=html;$('#modal').classList.remove('hidden')}
$('#fecharModal').onclick=()=>$('#modal').classList.add('hidden');

window.verOrcamento=async id=>{
  try{
    const [it,total]=await Promise.all([api(`/orcamentos/${id}/itens`),api(`/orcamentos/${id}/total`)]);
    modal('Orçamento',`${gruposItensHtml(it)}<h3>Total: ${moeda(total)}</h3><details><summary>Ver valores dos itens</summary><table><thead><tr><th>Item</th><th>Qtd.</th><th>Unitário</th><th>Subtotal</th></tr></thead><tbody>${it.map(i=>`<tr><td>${esc(itemIdentificacao(i.produto))}</td><td>${i.quantidade}</td><td>${moeda(i.valorUnitario)}</td><td>${moeda(Number(i.valorUnitario)*i.quantidade)}</td></tr>`).join('')}</tbody></table></details>`);
  }catch(e){toast(e.message)}
};

window.verVenda=async id=>{
  try{
    const venda=state.vendas.find(v=>v.codigoVenda===id)||historicoPage.content.find(v=>v.codigoVenda===id);
    const [it,r]=await Promise.all([api(`/vendas/${id}/itens`),api(`/vendas/${id}/resumo`)]);
    const isShopee=venda?.configuracaoVenda?.configuracaoShopee;
    const taxas=isShopee
      ? `<p><b>Taxas Shopee:</b> ${moeda(r.taxaShopee)}</p>`
      : `<p><b>Taxa percentual:</b> ${moeda(r.taxaPercentual)}</p><p><b>Taxa fixa:</b> ${moeda(r.taxaFixa)}</p>`;

    modal('Detalhes da venda',`
      <p><b>Status:</b> ${esc(r.status)}</p>
      <p><b>Faturamento:</b> ${moeda(r.faturamento)}</p>
      ${taxas}
      <p><b>Embalagem:</b> ${moeda(r.embalagem)}</p>
      <p><b>Frete pago pelo vendedor:</b> ${moeda(r.freteVendedor)}</p>
      <p><b>Despesas totais:</b> ${moeda(r.despesasTotais)}</p>
      <p><b>Resultado antes do custo de estoque:</b> ${moeda(r.lucro)}</p>
      ${gruposItensHtml(it)}
      <details><summary>Ver valores dos itens</summary>
        <table><thead><tr><th>Item</th><th>Qtd.</th><th>Venda un.</th><th>Subtotal</th></tr></thead><tbody>
        ${it.map(i=>`<tr><td>${esc(itemIdentificacao(i.produto))}</td><td>${i.quantidade}</td><td>${moeda(i.valorUnitario)}</td><td>${moeda(Number(i.valorUnitario)*i.quantidade)}</td></tr>`).join('')}
        </tbody></table>
      </details>
    `);
  }catch(e){toast(e.message)}
};

/* ---------- Eventos de filtros ---------- */
const productFilterIds=['#filtroProdutoTipo','#filtroProdutoAlbum','#filtroProdutoAno','#filtroProdutoSelecao','#filtroProdutoCategoria','#filtroProdutoEstoque','#filtroProdutoBusca'];
$('#aplicarPeriodoDashboard')?.addEventListener('click',()=>{
  if(val('#dashboardInicio')&&val('#dashboardFim')&&val('#dashboardInicio')>val('#dashboardFim')) return toast('A data inicial não pode ser posterior à data final.');
  dashboardPages.volume=0; dashboardPages.mais=0; dashboardPages.estoque=0;
  carregarDashboard();
});
$('#limparPeriodoDashboard')?.addEventListener('click',()=>{
  $('#dashboardInicio').value=''; $('#dashboardFim').value='';
  dashboardPages.volume=0; dashboardPages.mais=0; dashboardPages.estoque=0;
  carregarDashboard();
});

productFilterIds.forEach(id=>{const e=$(id);e?.addEventListener('change',()=>{updateFilterButton($('#filtrosProdutos'));carregarPaginaProdutos(0)});e?.addEventListener('input',()=>{if(e.tagName==='INPUT'){updateFilterButton($('#filtrosProdutos'));carregarPaginaProdutos(0)}})});

const historyFilterIds=['#historicoDataInicio','#historicoDataFim','#historicoStatus','#historicoCliente','#historicoCanal','#historicoTipoProduto'];
historyFilterIds.forEach(id=>{const e=$(id);e?.addEventListener('change',()=>{updateFilterButton($('#filtrosHistorico'));carregarHistorico(0)})});

const localFilterIds=[
  '#filtroAlbumNome','#filtroAlbumAno','#filtroAlbumTipo','#filtroCategoriaNome','#filtroClienteBusca',
  '#filtroPrecoAlbum','#filtroPrecoAno','#filtroPrecoCategoria','#filtroConfiguracaoNome','#filtroConfiguracaoShopee',
  '#filtroRegraShopee','#filtroOrcamentoCliente','#filtroOrcamentoData','#filtroOrcamentoMin','#filtroOrcamentoMax',
  '#filtroCompraDescricao','#filtroCompraData'
];
localFilterIds.forEach(id=>{const e=$(id);const event=e?.tagName==='SELECT'?'change':'input';e?.addEventListener(event,()=>{renderAll();const panel=e.closest('.table-filters');updateFilterButton(panel)})});

function clearFilters(panelId, ids, callback=renderAll){
  ids.forEach(id=>{const e=$(id);if(e){e.value='';syncSearchableSelect(e)}});
  updateFilterButton($(panelId));callback();
}
$('#limparFiltrosProdutos').onclick=()=>clearFilters('#filtrosProdutos',productFilterIds,()=>carregarPaginaProdutos(0));
$('#limparFiltrosAlbuns').onclick=()=>clearFilters('#filtrosAlbuns',['#filtroAlbumNome','#filtroAlbumAno','#filtroAlbumTipo']);
$('#limparFiltrosCategorias').onclick=()=>clearFilters('#filtrosCategorias',['#filtroCategoriaNome']);
$('#limparFiltrosClientes').onclick=()=>clearFilters('#filtrosClientes',['#filtroClienteBusca']);
$('#limparFiltrosPrecos').onclick=()=>clearFilters('#filtrosPrecos',['#filtroPrecoAlbum','#filtroPrecoAno','#filtroPrecoCategoria']);
$('#limparFiltrosConfiguracoes').onclick=()=>clearFilters('#filtrosConfiguracoes',['#filtroConfiguracaoNome','#filtroConfiguracaoShopee']);
$('#limparFiltrosRegrasShopee').onclick=()=>clearFilters('#filtrosRegrasShopee',['#filtroRegraShopee']);
$('#limparFiltrosOrcamentos').onclick=()=>clearFilters('#filtrosOrcamentos',['#filtroOrcamentoCliente','#filtroOrcamentoData','#filtroOrcamentoMin','#filtroOrcamentoMax']);
$('#limparFiltrosCompras').onclick=()=>clearFilters('#filtrosCompras',['#filtroCompraDescricao','#filtroCompraData']);
$('#limparFiltrosHistorico').onclick=()=>clearFilters('#filtrosHistorico',historyFilterIds,()=>carregarHistorico(0));

/* ---------- Inicialização ---------- */
atualizarCamposShopee();
carregarUsuario().then(loadAll);
