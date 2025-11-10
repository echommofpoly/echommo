// static/js/my-listings.js

function cancelListing(listingId) {
    if (!confirm('Bạn có chắc muốn hủy đăng bán này?')) {
        return;
    }
    
    fetch(`/marketplace/cancel/${listingId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            location.reload();
        } else {
            alert('Lỗi: ' + data.message);
        }
    })
    .catch(error => {
        alert('Có lỗi xảy ra: ' + error.message);
    });
}