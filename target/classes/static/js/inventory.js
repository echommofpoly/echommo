// static/js/inventory.js
// Cần nhúng common.js TRƯỚC file này

function filterItems(category, buttonElement) {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
     if (buttonElement) {
         buttonElement.classList.add('active');
     }

    const items = document.querySelectorAll('.inventory-item');
    let hasVisibleItems = false;
    items.forEach(item => {
        const itemCategory = item.dataset.category || 'other';
        if (category === 'all' || itemCategory === category) {
            item.style.display = 'flex';
            hasVisibleItems = true;
        } else {
            item.style.display = 'none';
        }
    });
}

async function equipItem(userItemId) {
    try {
        const result = await fetchApi(`/inventory/equip/${userItemId}`, { method: 'POST' });
        showNotification('success', result.message || 'Trang bị thành công!');
        updateItemUI(userItemId, { isEquipped: true });
    } catch (error) {
        showNotification('error', error.message || 'Lỗi khi trang bị vật phẩm.');
    }
}

async function unequipItem(userItemId) {
     try {
        const result = await fetchApi(`/inventory/unequip/${userItemId}`, { method: 'POST' });
        showNotification('success', result.message || 'Gỡ trang bị thành công!');
        updateItemUI(userItemId, { isEquipped: false });
    } catch (error) {
        showNotification('error', error.message || 'Lỗi khi gỡ trang bị.');
    }
}

async function useItem(userItemId) {
    try {
        const result = await fetchApi(`/inventory/use/${userItemId}`, { method: 'POST' });
        showNotification('success', result.message || 'Sử dụng vật phẩm thành công!');
         setTimeout(() => location.reload(), 1000);
    } catch (error) {
        showNotification('error', error.message || 'Lỗi khi sử dụng vật phẩm.');
    }
}

// ### HÀM NÀY PHẢI ĐÚNG ###
function sellItemPrompt(buttonElement) {
    const itemId = buttonElement.dataset.itemId;
    const maxQty = buttonElement.dataset.maxQty;
    const itemName = buttonElement.dataset.itemName;
    const encodedItemName = encodeURIComponent(itemName);
    location.href = `/marketplace/create?itemId=${itemId}&itemName=${encodedItemName}&maxQty=${maxQty}`;
}
// #########################

function updateItemUI(userItemId, changes) {
    const itemCard = document.querySelector(`.inventory-item button[data-item-id='${userItemId}']`)?.closest('.inventory-item') ||
                     document.querySelector(`.inventory-item button[onclick*="(${userItemId})"]`)?.closest('.inventory-item');
    if (!itemCard) return;

    if (changes.isEquipped !== undefined) {
        const equipBtn = itemCard.querySelector('button[onclick*="equipItem"]');
        const unequipBtn = itemCard.querySelector('button[onclick*="unequipItem"]');
        const overlay = itemCard.querySelector('.equipped-overlay');
        if (changes.isEquipped) {
            if (equipBtn) equipBtn.style.display = 'none';
            if (unequipBtn) unequipBtn.style.display = 'inline-flex';
            if (overlay) overlay.style.opacity = '1';
        } else {
             if (equipBtn) equipBtn.style.display = 'inline-flex';
             if (unequipBtn) unequipBtn.style.display = 'none';
             if (overlay) overlay.style.opacity = '0';
        }
    }
    if (changes.quantity !== undefined) {
         const qtyBadge = itemCard.querySelector('.quantity-badge span');
         if (qtyBadge) {
             if (changes.quantity <= 0) {
                 itemCard.remove();
             } else {
                 qtyBadge.textContent = changes.quantity;
                 const sellBtn = itemCard.querySelector('button[onclick*="sellItemPrompt"]'); // Sửa selector này nếu cần
                 if(sellBtn) {
                     sellBtn.dataset.maxQty = changes.quantity;
                 }
             }
         }
    }
}

// ### PHẦN NÀY PHẢI ĐÚNG ###
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        let category = btn.dataset.category; // Thử lấy từ data attribute trước
        if (!category) { // Nếu không có, thử lấy từ onclick cũ
            const onclickAttr = btn.getAttribute('onclick');
            if (onclickAttr && onclickAttr.includes('filterItems')) {
                 const match = onclickAttr.match(/'([^']+)'/);
                 if (match) category = match[1];
            }
        }
        // Gán lại onclick chuẩn
        if (category) {
            btn.onclick = (event) => filterItems(category, event.currentTarget);
        } else {
             console.warn("Could not determine category for button:", btn); // Báo lỗi nếu không xác định được category
        }
    });

    // Gọi filterItems('all') lần đầu
    const firstTab = document.querySelector('.tab-btn.active') || document.querySelector('.tab-btn');
    if (firstTab) {
        let initialCategory = firstTab.dataset.category;
         if (!initialCategory) {
            const onclickAttr = firstTab.getAttribute('onclick');
            if (onclickAttr && onclickAttr.includes('filterItems')) {
                 const match = onclickAttr.match(/'([^']+)'/);
                 if (match) initialCategory = match[1];
            }
        }
        filterItems(initialCategory || 'all', firstTab);
    }
});
// #########################