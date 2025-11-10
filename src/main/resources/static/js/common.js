// common.js

/**
 * Hiển thị thông báo toast ngắn gọn.
 * @param {'success' | 'error'} type Loại thông báo
 * @param {string} message Nội dung thông báo
 * @param {number} duration Thời gian hiển thị (ms)
 */
function showNotification(type, message, duration = 3000) {
  const notif = document.createElement('div');
  notif.className = `notification notification-${type}`;
  notif.textContent = message;

  // Thêm style nếu chưa có trong CSS chung
  notif.style.position = 'fixed';
  notif.style.top = '20px';
  notif.style.right = '20px';
  // ... (các style khác cho padding, background, color,...)

  document.body.appendChild(notif);

  setTimeout(() => {
    notif.style.animation = 'slideOut 0.3s ease forwards'; // Thêm forwards
    setTimeout(() => notif.remove(), 300);
  }, duration);
}

/**
 * Hàm tiện ích gọi API Fetch với xử lý lỗi cơ bản.
 * @param {string} url URL của API
 * @param {object} options Các tùy chọn của Fetch (method, headers, body,...)
 * @returns {Promise<any>} Promise chứa dữ liệu JSON trả về nếu thành công
 * @throws {Error} Nếu fetch thất bại hoặc response không ok hoặc không phải JSON
 */
async function fetchApi(url, options = {}) {
  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      // Cố gắng đọc lỗi từ server nếu có
      let errorData;
      try {
        errorData = await response.json();
      } catch (e) {
        // Không phải JSON error
      }
      const errorMessage = errorData?.message || `HTTP error! status: ${response.status}`;
      throw new Error(errorMessage);
    }

    // Kiểm tra content-type trước khi parse JSON
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
       const data = await response.json();
       if (!data.success && data.message) { // Kiểm tra cấu trúc ApiResponse
           throw new Error(data.message);
       }
       return data; // Trả về toàn bộ response data (bao gồm success, message, data)
    } else {
       // Xử lý trường hợp response không phải JSON nếu cần (ví dụ: chỉ trả về text)
       // Hoặc throw lỗi nếu luôn mong đợi JSON
       console.warn(`Response from ${url} was not JSON.`);
       return null; // Hoặc trả về response.text() nếu muốn
    }

  } catch (error) {
    console.error(`Fetch error for ${url}:`, error);
    // Ném lại lỗi để hàm gọi có thể bắt và hiển thị thông báo
    throw error; // Quan trọng: Ném lại lỗi
  }
}

// --- Hàm cập nhật Header ---
function updateHeaderHP(value) {
    const element = document.getElementById('headerHp');
    if(element) element.textContent = value;
}
function updateHeaderEnergy(value) {
    const element = document.getElementById('headerEnergy');
    if(element) element.textContent = value;
}
function updateHeaderGold(value) {
    const element = document.getElementById('headerGold');
    if (element) {
        // Định dạng lại số tiền nếu cần
        try {
            const formattedValue = parseFloat(value).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
            element.textContent = formattedValue;
        } catch (e) {
            element.textContent = value; // Hiển thị giá trị gốc nếu lỗi format
        }
    }
}

// Thêm CSS animations vào head nếu chưa có trong CSS chung
document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('common-animations')) {
        const style = document.createElement('style');
        style.id = 'common-animations';
        style.textContent = `
          @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
          @keyframes slideOut { to { transform: translateX(100%); opacity: 0; } }
        `;
        document.head.appendChild(style);
    }
});