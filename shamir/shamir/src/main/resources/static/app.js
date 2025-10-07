// Generate Ethereum Wallet
async function generateWallet() {
    const password = document.getElementById('password').value;
    const response = await fetch('/api/generateWallet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `password=${encodeURIComponent(password)}`
    });
    const data = await response.text();
    const [publicKey, privateKey] = data.split(', PrivateKey: ');
    document.getElementById('walletDetails').innerHTML = `
        <p>Wallet generated successfully.</p>
        <p><strong>PublicKey:</strong> <span class="key">${publicKey.split('PublicKey: ')[1]}</span></p>
        <p><strong>PrivateKey:</strong> <span class="key">${privateKey}</span></p>
    `;
}

// Split Private Key
async function splitPrivateKey() {
    const privateKey = document.getElementById('privateKey').value;
    const n = document.getElementById('n').value;
    const k = document.getElementById('k').value;
    const response = await fetch('/api/splitPrivateKey', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `privateKey=${encodeURIComponent(privateKey)}&n=${n}&k=${k}`
    });
    const data = await response.json();
    let formattedShares = "";
    for (const [key, value] of Object.entries(data)) {
        formattedShares += `<p><strong>Part ${key}:</strong> <span class="key">${value}</span></p>`;
    }
    document.getElementById('splitDetails').innerHTML = formattedShares;
}

// Recover Private Key
async function recoverPrivateKey() {
    const sharesInput = document.getElementById('shares').value;
    const sharesArray = sharesInput.split(",").map(share => share.trim());
    const shares = sharesArray.reduce((acc, share, index) => {
        acc[index + 1] = share;
        return acc;
    }, {});

    const response = await fetch('/api/recoverPrivateKey', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(shares)
    });
    const data = await response.text();
    document.getElementById('recoveryDetails').innerText = 'Recovered Private Key: ' + data;
}

// Sign Message
async function signMessage() {
    const privateKey = prompt("Enter Private Key to sign your message:");
    const message = document.getElementById('message').value;
    const response = await fetch('/api/signMessage', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `privateKey=${encodeURIComponent(privateKey)}&message=${encodeURIComponent(message)}`
    });
    const data = await response.json();
    document.getElementById('signatureDetails').innerText = 'Signature: ' + JSON.stringify(data);
}

// Verify Signature
async function verifySignature() {
    const message = document.getElementById('verifyMessage').value;
    const publicKey = document.getElementById('publicKey').value;
    const signatureInput = document.getElementById('signature').value;
    const signature = JSON.parse(signatureInput);

    const response = await fetch('/api/verifySignature', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message, signature, publicKey })
    });
    const result = await response.json();
    document.getElementById('verificationDetails').innerText = 'Verification Result: ' + (result.isVerified ? 'Success' : 'Failure');
}
