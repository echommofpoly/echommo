// static/js/create-listing.js

let currentTab = 'player';
let selectedItem = null;

function switchTab(tab) {
    currentTab = tab;
    
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Update tab contents
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(`${tab}-tab`).classList.add('active');
}

function selectItemFromInventory(tab) {
    currentTab = tab;
    
    // Fetch inventory
    fetch('/api/inventory/items')
        .then(response => response.json())
        .then(data => {
            showItemModal(data);
        })
        .catch(error => {
            alert('Lỗi khi tải kho đồ: ' + error.message);
        });
}

function showItemModal(items) {
    const modal = document.getElementById('itemModal');
    const itemList = document.getElementById('itemList');
    
    itemList.innerHTML = '';
    
    items.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'item-list-item';
        itemDiv.innerHTML = `
            <img src="${item.imageUrl || '/images/items/default.png'}" alt="${item.name}">
            <h4>${item.name}</h4>
            <p>x${item.quantity}</p>
            <span class="rarity-badge rarity-${item.rarity.toLowerCase()}">${item.rarity}</span>
        `;
        itemDiv.onclick = () => selectItem(item);
        itemList.appendChild(itemDiv);
    });
    
    modal.classList.add('show');
}

function closeModal() {
    document.getElementById('itemModal').classList.remove('show');
}

function selectItem(item) {
    selectedItem = item;
    
    // Update hidden input
    document.getElementById(`${currentTab}-itemId`).value = item.itemId;
    
    // Show selected item
    const selectedDiv = document.getElementById(`${currentTab}-selected`);
    selectedDiv.innerHTML = `
        <div style="display: flex; align-items: center; gap: 1rem;">
            <img src="${item.imageUrl || '/images/items/default.png'}" 
                 style="width: 50px; height: 50px; object-fit: contain;" 
                 alt="${item.name}">
            <div>
                <h4>${item.name}</h4>
                <p>Có sẵn: ${item.quantity}</p>
                <p>Giá gốc: ${item.basePrice} vàng</p>
            </div>
        </div>
    `;
    selectedDiv.classList.add('show');
    
    // Set max quantity
    document.getElementById(`${currentTab}-quantity`).max = item.quantity;
    document.getElementById(`${currentTab}-quantity`).value = 1;
    
    // If system tab, calculate estimate
    if (currentTab === 'system') {
        updateSystemEstimate();
    }
    
    closeModal();
}

function updateSystemEstimate() {
    if (!selectedItem) return;
    
    const quantity = parseInt(document.getElementById('system-quantity').value) || 1;
    const basePrice = parseFloat(selectedItem.basePrice);
    const estimate = (basePrice * quantity * 0.7).toFixed(2);
    
    const estimateDiv = document.getElementById('system-estimate');
    estimateDiv.innerHTML = `
        <p><strong>Ước tính nhận được:</strong></p>
        <p style="font-size: 1.5rem; color: #f59e0b;">${estimate} vàng</p>
        <small style="color: #94a3b8;">Giá bán = 70% giá gốc</small>
    `;
    estimateDiv.classList.add('show');
}

// Event listeners for quantity change
document.addEventListener('DOMContentLoaded', () => {
    const systemQty = document.getElementById('system-quantity');
    if (systemQty) {
        systemQty.addEventListener('input', updateSystemEstimate);
    }
});

// Form submissions
document.getElementById('playerListingForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = {
        itemId: parseInt(document.getElementById('player-itemId').value),
        quantity: parseInt(document.getElementById('player-quantity').value),
        price: parseFloat(document.getElementById('player-price').value),
        listingType: 'Player'
    };
    
    try {
        const response = await fetch('/marketplace/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('Đăng bán thành công!');
            location.href = '/marketplace/my-listings';
        } else {
            alert('Lỗi: ' + result.message);
        }
    } catch (error) {
        alert('Có lỗi xảy ra: ' + error.message);
    }
});

document.getElementById('systemSaleForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = {
        itemId: parseInt(document.getElementById('system-itemId').value),
        quantity: parseInt(document.getElementById('system-quantity').value)
    };
    
    try {
        const response = await fetch('/marketplace/sell-to-system', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert(`Bán thành công! Nhận được ${result.data} vàng`);
            location.href = '/inventory';
        } else {
            alert('Lỗi: ' + result.message);
        }
    } catch (error) {
        alert('Có lỗi xảy ra: ' + error.message);
    }
});

document.getElementById('adminListingForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = {
        itemId: parseInt(document.getElementById('admin-itemId').value),
        quantity: parseInt(document.getElementById('admin-quantity').value),
        price: parseFloat(document.getElementById('admin-price').value),
        listingType: 'Admin'
    };
    
    try {
        const response = await fetch('/marketplace/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('Tạo Admin listing thành công!');
            location.href = '/marketplace';
        } else {
            alert('Lỗi: ' + result.message);
        }
    } catch (error) {
        alert('Có lỗi xảy ra: ' + error.message);
    }
});