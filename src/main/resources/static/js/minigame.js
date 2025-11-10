let isAdventuring = false;

const characterEl = document.getElementById('character');
const accessEl = document.getElementById('access');
const messageBox = document.getElementById('messageBox');
const adventureBtn = document.getElementById('adventureBtn');
const gatherBtn = document.getElementById('gatherBtn');
const restBtn = document.getElementById('restBtn');

function updateStatsUI(stats) {
    if (stats.health !== null) document.getElementById('playerHp').textContent = stats.health;
    if (stats.energy !== null) document.getElementById('playerEnergy').textContent = stats.energy;
    if (stats.gold !== null) document.getElementById('playerGold').textContent = stats.gold;
}

function showMessage(text, duration = 3000) {
    messageBox.textContent = text;
    messageBox.style.opacity = 1;
    setTimeout(() => messageBox.style.opacity = 0.8, duration);
}

function randBetween(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function lockButtons(locked) {
    isAdventuring = locked;
    adventureBtn.disabled = locked;
    gatherBtn.disabled = locked;
    restBtn.disabled = locked;
}

adventureBtn.addEventListener('click', async () => {
    if (isAdventuring) return;
    lockButtons(true);
    showMessage('🧭 Đang phiêu lưu...');

    try {
        const response = await fetch('/api/minigame/adventure', { method: 'POST' });
        const result = await response.json();

        if (!result.success) {
            showMessage('❌ ' + result.message);
            lockButtons(false);
            return;
        }

        const data = result.data;
        updateStatsUI(data.updatedStats);

        // Tiến tới đảo
        const targetX = randBetween(280, 400);
        const homeX = 48;

        accessEl.style.left = (targetX + 100) + 'px';
        accessEl.style.opacity = '1';
        accessEl.textContent = data.encounterIcon || '🏝️';

        characterEl.classList.add('walk');
        characterEl.style.left = targetX + 'px';
        await delay(1200);

        accessEl.classList.add('shake');
        await delay(700);
        accessEl.classList.remove('shake');

        showMessage('✨ ' + data.message);
        characterEl.classList.remove('walk');
        characterEl.classList.add('returning');
        await delay(400);

        // Quay về
        characterEl.style.left = homeX + 'px';
        await delay(1200);
        characterEl.classList.remove('returning');
        accessEl.style.opacity = '0';

        lockButtons(false);
    } catch (error) {
        showMessage('💥 Lỗi kết nối: ' + error.message);
        lockButtons(false);
    }
});

gatherBtn.addEventListener('click', () => location.href = '/gathering/stone');

restBtn.addEventListener('click', async () => {
    if (isAdventuring) return;
    lockButtons(true);

    try {
        const response = await fetch('/api/minigame/rest', { method: 'POST' });
        const result = await response.json();

        if (result.success) {
            updateStatsUI(result.data);
            showMessage('😴 ' + result.message);
        } else {
            showMessage('⚡ ' + result.message);
        }
    } catch (error) {
        showMessage('💥 Lỗi kết nối: ' + error.message);
    }
    lockButtons(false);
});

function useQuickPotion() {
    showMessage('💊 Tính năng đang phát triển (cần gọi API kho đồ).');
}

showMessage('🎮 Bắt đầu phiêu lưu!');
