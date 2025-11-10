document.addEventListener('DOMContentLoaded', () => {
    
    const gatherPanel = document.querySelector('.gather-panel');
    const gatherBtn = document.getElementById('gatherBtn');
    const gatherLog = document.getElementById('gatherLog');
    
    // Header Stats
    const headerHp = document.getElementById('headerHp');
    const headerEnergy = document.getElementById('headerEnergy');
    const headerGold = document.getElementById('headerGold');
    
    // Skill Info
    const gatherLv = document.getElementById('gatherLv');
    const progressPercent = document.getElementById('progressPercent');
    const progressFill = document.getElementById('progressFill');

    let isGathering = false;
    
    // Lấy resourceType từ data-attribute
    const resourceType = gatherPanel.dataset.resourceType;
    if (!resourceType) {
        gatherLog.textContent = "Lỗi: Không tìm thấy resourceType";
        return;
    }

    gatherBtn.addEventListener('click', async () => {
        if (isGathering) return;
        isGathering = true;
        gatherBtn.disabled = true;
        gatherLog.textContent = "Đang thu thập...";

        try {
            // 1. Tạo form data để gửi
            const formData = new URLSearchParams();
            formData.append('resourceType', resourceType);

            // 2. Gọi API
            const response = await fetch('/api/gathering/gather', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData
            });

            const result = await response.json();

            // 3. Xử lý kết quả
            if (result.success) {
                const data = result.data;
                
                // Cập nhật log
                gatherLog.textContent = data.message;
                if(data.levelUp) {
                    gatherLog.textContent += " CHÚC MỪNG LÊN CẤP!";
                }

                // Cập nhật Header
                headerEnergy.textContent = data.playerEnergy;
                headerGold.textContent = data.playerGold;

                // Cập nhật Panel
                gatherLv.textContent = data.level;
                progressPercent.textContent = data.expPercent.toFixed(1) + '%';
                progressFill.style.width = data.expPercent + '%';

            } else {
                gatherLog.textContent = "Lỗi: " + result.message;
            }

        } catch (error) {
            gatherLog.textContent = "Lỗi kết nối: " + error.message;
        }

        isGathering = false;
        gatherBtn.disabled = false;
    });
});