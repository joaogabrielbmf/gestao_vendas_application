const loginBox = document.querySelector('#loginBox');
const cadastroBox = document.querySelector('#cadastroBox');
const authMsg = document.querySelector('#authMsg');

function abrirLogin(mensagem='') {
  cadastroBox.classList.add('hidden');
  loginBox.classList.remove('hidden');
  authMsg.textContent = mensagem;
}

function abrirCadastro() {
  loginBox.classList.add('hidden');
  cadastroBox.classList.remove('hidden');
  authMsg.textContent = '';
}

document.querySelector('#mostrarCadastro').onclick = abrirCadastro;
document.querySelector('#mostrarLogin').onclick = () => abrirLogin();

document.querySelector('#cadastroForm').onsubmit = async e => {
  e.preventDefault();

  if (cadSenha.value !== cadConfirmarSenha.value) {
    authMsg.textContent = 'As senhas não coincidem.';
    return;
  }

  const r = await fetch('/auth/cadastro', {
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({nome:cadNome.value,email:cadEmail.value,senha:cadSenha.value})
  });

  let d = {};
  try { d = await r.json(); } catch {}

  if (r.ok) {
    cadastroForm.reset();
    abrirLogin('Conta criada com sucesso. Agora faça login.');
  } else {
    authMsg.textContent = d.message || 'Erro ao cadastrar.';
  }
};

if (location.search.includes('erro=1')) {
  abrirLogin('E-mail ou senha inválidos.');
}
