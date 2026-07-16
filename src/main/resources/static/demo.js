const api = "/api/fido";
const output = document.querySelector("#result");
const username = () => document.querySelector("#username").value.trim();
const displayName = () => document.querySelector("#displayName").value.trim();

function setResult(message, success = false) {
  output.textContent = message;
  output.className = success ? "success" : "failure";
}

function fromBase64Url(value) {
  const padded = value.replace(/-/g, "+").replace(/_/g, "/").padEnd(Math.ceil(value.length / 4) * 4, "=");
  const binary = atob(padded);
  return Uint8Array.from(binary, (character) => character.charCodeAt(0)).buffer;
}

function toBase64Url(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  bytes.forEach((byte) => { binary += String.fromCharCode(byte); });
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function registrationOptions(publicKey) {
  return {
    ...publicKey,
    challenge: fromBase64Url(publicKey.challenge),
    user: { ...publicKey.user, id: fromBase64Url(publicKey.user.id) },
    excludeCredentials: (publicKey.excludeCredentials || []).map((credential) => ({ ...credential, id: fromBase64Url(credential.id) })),
  };
}

function assertionOptions(publicKey) {
  return {
    ...publicKey,
    challenge: fromBase64Url(publicKey.challenge),
    allowCredentials: (publicKey.allowCredentials || []).map((credential) => ({ ...credential, id: fromBase64Url(credential.id) })),
  };
}

function credentialJson(credential) {
  const response = credential.response;
  const json = {
    id: credential.id,
    rawId: toBase64Url(credential.rawId),
    type: credential.type,
    response: { clientDataJSON: toBase64Url(response.clientDataJSON) },
  };
  if (response.attestationObject) {
    json.response.attestationObject = toBase64Url(response.attestationObject);
    if (response.getTransports) json.response.transports = response.getTransports();
  } else {
    json.response.authenticatorData = toBase64Url(response.authenticatorData);
    json.response.signature = toBase64Url(response.signature);
    if (response.userHandle) json.response.userHandle = toBase64Url(response.userHandle);
  }
  return json;
}

async function request(path, body, method = "POST") {
  const response = await fetch(`${api}${path}`, {
    method,
    headers: { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || `HTTP ${response.status}`);
  }
  return response.status === 204 ? undefined : response.json();
}

async function register() {
  const options = await request("/registration/options", { username: username(), displayName: displayName() });
  const credential = await navigator.credentials.create({ publicKey: registrationOptions(options.publicKey) });
  await request("/registration/verify", { ceremonyId: options.ceremonyId, credentialJson: JSON.stringify(credentialJson(credential)) });
  setResult("등록 성공 · 사용자 검증: 인증기 응답에 따라 완료", true);
  loadCredentials();
}

async function authenticate(discoverable = false) {
  const options = await request(
    discoverable ? "/authentication/discoverable/options" : "/authentication/options",
    discoverable ? undefined : { username: username() },
  );
  const credential = await navigator.credentials.get({ publicKey: assertionOptions(options.publicKey) });
  const result = await request(
    discoverable ? "/authentication/discoverable/verify" : "/authentication/verify",
    { ceremonyId: options.ceremonyId, credentialJson: JSON.stringify(credentialJson(credential)) },
  );
  setResult(`인증 성공 · 사용자: ${result.username} · 사용자 검증: ${result.userVerified ? "완료" : "미완료"}`, true);
  loadCredentials();
}

async function loadCredentials() {
  const credentials = await request(`/users/${encodeURIComponent(username())}/credentials`, undefined, "GET");
  document.querySelector("#credentials").replaceChildren(...credentials.map((credential) => {
    const item = document.createElement("li");
    item.textContent = `${credential.credentialId} · counter ${credential.signatureCount} · 마지막 인증 ${credential.lastAuthenticatedAt || "없음"}`;
    const remove = document.createElement("button");
    remove.textContent = "삭제";
    remove.onclick = async () => { await request(`/users/${encodeURIComponent(username())}/credentials/${encodeURIComponent(credential.credentialId)}`, undefined, "DELETE"); loadCredentials(); };
    item.append(" ", remove);
    return item;
  }));
}

async function run(action) {
  try { output.className = ""; output.textContent = "처리 중…"; await action(); } catch (error) { setResult(`실패: ${error.message}`); }
}

document.querySelector("#register").onclick = () => run(register);
document.querySelector("#authenticate").onclick = () => run(() => authenticate(false));
document.querySelector("#discoverable").onclick = () => run(() => authenticate(true));
document.querySelector("#loadCredentials").onclick = () => run(loadCredentials);
document.querySelector("#environment").textContent = window.PublicKeyCredential && window.isSecureContext ? "WebAuthn을 사용할 수 있습니다." : "WebAuthn은 HTTPS 또는 localhost의 지원 브라우저에서만 사용할 수 있습니다.";
