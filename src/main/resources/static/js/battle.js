document.addEventListener('DOMContentLoaded', () => {

    // === DOM Elements ===
    const container = document.querySelector('.battle-container');
    const battleLog = document.getElementById('battleLog');
    
    // HP Bars
    const playerHPBar = document.getElementById('playerHPBar');
    const playerHPText = document.getElementById('playerHPText');
    const enemyHPBar = document.getElementById('enemyHPBar');
    const enemyHPText = document.getElementById('enemyHPText');

    // Chars
    const playerChar = document.getElementById('playerChar');
    const enemyChar = document.getElementById('enemyChar');

    // Button Groups
    const actionButtons = document.getElementById('actionButtons');
    const potionList = document.getElementById('potionList');
    const resultScreen = document.getElementById('resultScreen');

    // Buttons
    const attackBtn = document.getElementById('attackBtn');
    const useItemBtn = document.getElementById('useItemBtn');
    const fleeBtn = document.getElementById('fleeBtn');
    const cancelPotionBtn = document.getElementById('cancelPotionBtn');

    // Result Screen
    const resultTitle = document.getElementById('resultTitle');
    const resultRewards = document.getElementById('resultRewards');

    // === Battle State ===
    const enemyId = container.dataset.enemyId;
    let playerMaxHp = parseFloat(container.dataset.playerHp);
    let playerHp = playerMaxHp;
    let enemyMaxHp = parseFloat(container.dataset.enemyHp);
    let enemyHp = enemyMaxHp;

    let isActionLocked = false;

    // === Functions ===

    function lockActions(locked) {
        isActionLocked = locked;
        attackBtn.disabled = locked;
        useItemBtn.disabled = locked;
        fleeBtn.disabled = locked;
    }

    function addLog(message) {
        battleLog.innerHTML = message; // Chỉ hiển thị log mới nhất
    }

    function updateHPBars() {
        playerHPText.textContent = `HP: ${playerHp}/${playerMaxHp}`;
        playerHPBar.style.width = `${(playerHp / playerMaxHp) * 100}%`;
        enemyHPText.textContent = `HP: ${enemyHp}/${enemyMaxHp}`;
        enemyHPBar.style.width = `${(enemyHp / enemyMaxHp) * 100}%`;
    }

    async function handleApiResponse(response) {
        const result = await response.json();

        if (!result.success) {
            addLog("Lỗi: " + result.message);
            lockActions(false);
            return;
        }

        const data = result.data;
        
        // Cập nhật HP
        playerHp = data.playerHp;
        enemyHp = data.enemyHp;
        updateHPBars();

        // Hiển thị log
        addLog(data.log.join(' ')); // Nối các log lại

        // Cập nhật số lượng Potion (nếu dùng item)
        if (data.userItemId) {
            const potionBtn = document.querySelector(`.btn-potion[data-item-id="${data.userItemId}"]`);
            if (potionBtn) {
                const qtySpan = potionBtn.querySelector('.potion-qty');
                qtySpan.textContent = data.updatedPotionQty;
                if (data.updatedPotionQty <= 0) {
                    potionBtn.disabled = true;
                }
            }
        }
        
        // Xử lý kết quả (thắng, thua, bỏ chạy)
        if (data.battleStatus > 0) {
            lockActions(true);
            await delay(1500);
            showResult(data.battleStatus, data);
        } else {
            lockActions(false);
        }
    }

    function showResult(status, data) {
        actionButtons.classList.add('hidden');
        potionList.classList.add('hidden');
        resultScreen.classList.remove('hidden');

        if (status === 1) { // Thắng
            resultTitle.textContent = "CHIẾN THẮNG!";
            resultTitle.style.color = "#6ee7b7";
            resultRewards.innerHTML = `+${data.rewardExp} EXP<br>+${data.rewardGold} Vàng`;
        } else if (status === 2) { // Thua
            resultTitle.textContent = "BẠN ĐÃ THUA!";
            resultTitle.style.color = "#ef4444";
            resultRewards.innerHTML = "May mắn lần sau...";
        } else if (status === 3) { // Bỏ chạy
            resultTitle.textContent = "ĐÃ BỎ CHẠY!";
            resultTitle.style.color = "#f59e0b";
            resultRewards.innerHTML = "";
        }
    }
    
    function delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    // === Event Listeners ===

    attackBtn.addEventListener('click', async () => {
        if (isActionLocked) return;
        lockActions(true);
        addLog("Đang tấn công...");

        const formData = new URLSearchParams();
        formData.append('enemyId', enemyId);
        formData.append('enemyHp', enemyHp);
        
        const response = await fetch('/api/battle/attack', {
            method: 'POST',
            body: formData
        });
        
        handleApiResponse(response);
    });

    fleeBtn.addEventListener('click', async () => {
        if (isActionLocked) return;
        lockActions(true);
        
        const response = await fetch('/api/battle/flee', { method: 'POST' });
        handleApiResponse(response);
    });

    useItemBtn.addEventListener('click', () => {
        actionButtons.classList.add('hidden');
        potionList.classList.remove('hidden');
    });

    cancelPotionBtn.addEventListener('click', () => {
        actionButtons.classList.remove('hidden');
        potionList.classList.add('hidden');
    });

    // Thêm listener cho tất cả các nút potion
    document.querySelectorAll('.btn-potion').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (isActionLocked) return;
            lockActions(true);
            addLog("Đang dùng Potion...");

            const userItemId = btn.dataset.itemId;
            
            const formData = new URLSearchParams();
            formData.append('userItemId', userItemId);
            formData.append('enemyId', enemyId);
            formData.append('enemyHp', enemyHp);

            const response = await fetch('/api/battle/use-item', {
                method: 'POST',
                body: formData
            });

            // Sau khi dùng item, quay lại màn hình action chính
            actionButtons.classList.remove('hidden');
            potionList.classList.add('hidden');

            handleApiResponse(response);
        });
    });

});