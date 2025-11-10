// static/js/marketplace.js
// Cần nhúng common.js TRƯỚC file này

async function buyItem(listingId) {
    if (!confirm('Bạn có chắc muốn mua vật phẩm này?')) {
        return;
    }

    try {
        const result = await fetchApi('/marketplace/buy', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ listingId: listingId })
        });

        showNotification('success', result.message || 'Mua vật phẩm thành công!');
        // Cập nhật số dư header nếu API trả về
        // if(result.data && result.data.newBalance !== undefined) {
        //    updateHeaderGold(result.data.newBalance);
        // }
        setTimeout(() => location.reload(), 1500); // Tải lại trang

    } catch (error) {
        showNotification('error', error.message || 'Có lỗi xảy ra khi mua hàng.');
    }
}

// Hàm showNotification đã ở trong common.js
// Animation CSS đã ở trong common.css