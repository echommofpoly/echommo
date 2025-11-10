document.addEventListener("DOMContentLoaded", () => {
    // === KHAI BÁO CÁC PHẦN TỬ (Sử dụng document.getElementById trực tiếp khi cần) ===
    const panel = document.getElementById("friendPanel");
    const openBtn = document.getElementById("openFriendPanelHeader");
    const closeBtn = document.getElementById("closeFriendPanel");
    const friendForm = document.getElementById("friendForm");
    const messageEl = document.getElementById("friendMessage");
    
    // Account Menu (FIXED: Menu sẽ xổ xuống)
    const accountMenu = document.querySelector(".account-menu");
    const accountBtn = document.querySelector(".account-btn");
    
    // Chat Panel (Các phần tử này nằm ở Home.html)
    const chatPanel = document.getElementById("chatPanel");
    const currentReceiverUsername = document.getElementById("currentReceiverUsername");


    // === LOGIC MENU TÀI KHOẢN (FIXED) ===
    if (accountMenu && accountBtn) {
        accountBtn.addEventListener("click", (e) => {
            e.stopPropagation(); 
            accountMenu.classList.toggle("open"); // FIXED: Menu sẽ xổ xuống
        });

        document.addEventListener("click", (e) => {
            if (!accountMenu.contains(e.target)) {
                accountMenu.classList.remove("open");
            }
        });
    }

    // === LOGIC FRIEND PANEL VÀ GỬI LỜI MỜI ===
    if (panel && openBtn && closeBtn) {
        openBtn.addEventListener("click", () => {
            panel.classList.add("open");
            if (chatPanel) {
                chatPanel.classList.remove("open"); // Đóng chat khi mở friend list
            }
        });
        closeBtn.addEventListener("click", () => panel.classList.remove("open"));
    }

    if (friendForm) {
        friendForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const username = document.getElementById("friendUsername").value.trim();
            if (!username) return;

            messageEl.style.color = "gray";
            messageEl.textContent = "⏳ Đang gửi lời mời...";

            try {
                const response = await fetch("/friends/add", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: new URLSearchParams({ username })
                });
                const result = await response.json();
                messageEl.textContent = result.message;
                messageEl.style.color = result.success ? "green" : "red";
                friendForm.reset();
                
                if (result.success) {
                   setTimeout(() => { location.reload(); }, 1500); 
                }
            } catch {
                messageEl.textContent = "❌ Lỗi kết nối.";
                messageEl.style.color = "red";
            }
        });
    }

    // Global Functions (respondFriend, deleteFriend)
    window.respondFriend = async (friendshipId, accept) => {
        try {
            const res = await fetch("/friends/respond?friendshipId=" + friendshipId + "&accept=" + accept, {
                method: "POST"
            });
            const text = await res.text();
            alert(text);
            location.reload(); 
        } catch {
            alert("❌ Lỗi kết nối.");
        }
    };
    
    window.deleteFriend = async (friendshipId) => {
        if (!confirm("Bạn có chắc chắn muốn xóa bạn bè này không?")) {
            return;
        }
        alert(`Đang xóa kết bạn với ID: ${friendshipId}. Cần triển khai API xóa backend.`);
    };


    // === LOGIC CHAT ===
    if (chatPanel && currentReceiverUsername) {
        
        const chatFriendNameEl = document.getElementById("chatFriendName");
        const closeChatBtn = document.getElementById("closeChatPanel");
        const messageInput = document.getElementById("messageInput");
        const sendMessageBtn = document.getElementById("sendMessageBtn");

        function appendMessage(content, isSelf, isError = false) {
            const chatMessagesEl = document.getElementById("chatMessages");
            if (!chatMessagesEl) return;
            
            const item = document.createElement("div");
            item.className = `message-item ${isSelf ? 'self' : 'friend'} ${isError ? 'error' : ''}`;
            
            const p = document.createElement("p");
            p.textContent = content;
            
            const time = document.createElement("span");
            time.className = "timestamp";
            time.textContent = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
            
            item.appendChild(p);
            item.appendChild(time);
            chatMessagesEl.appendChild(item);
            chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight;
        }

        async function loadChatHistory(friendUsername) {
            const chatMessagesEl = document.getElementById("chatMessages");
            if (!chatMessagesEl) return;
            
            chatMessagesEl.innerHTML = ''; 

            if (!friendUsername || friendUsername === '[Lỗi User]') {
                appendMessage("❌ Lỗi dữ liệu người bạn. Vui lòng thử tải lại trang.", false, true);
                return;
            }

            // Gọi API thật để tải lịch sử
            try {
                const response = await fetch(`/chat/history/${friendUsername}`);
                if (!response.ok) throw new Error('Failed to load history');
                
                const history = await response.json();
                
                if (history.length === 0) {
                    appendMessage("Bắt đầu cuộc trò chuyện. Chưa có tin nhắn nào.", false, false);
                } else {
                    history.forEach(msg => {
                        // Logic hiển thị tạm thời: So sánh với giá trị đang được lưu trong trường ẩn
                        const isSelf = msg.sender.username === currentReceiverUsername.value; 
                        
                        appendMessage(msg.content, isSelf);
                    });
                }
                
            } catch (error) {
                appendMessage("❌ Lỗi kết nối API lịch sử chat.", false, true);
            }
        }

        // 💡 HÀM MỞ CHAT (FIXED LỖI NULL)
        window.openChat = async (rawFriendUsername) => {
            const friendUsername = rawFriendUsername ? rawFriendUsername.trim() : null;
            
            // Kiểm tra nghiêm ngặt: Chặn mọi chuỗi rỗng/null/lỗi
            if (!friendUsername || friendUsername === 'null' || friendUsername === '') {
                alert("Lỗi: Không xác định được tên người bạn để mở chat.");
                console.error("openChat: Invalid friendUsername:", rawFriendUsername);
                return; 
            }

            chatFriendNameEl.textContent = `Chat với ${friendUsername}`;
            currentReceiverUsername.value = friendUsername;
            
            if (panel) panel.classList.remove("open");
            chatPanel.classList.add("open");

            await loadChatHistory(friendUsername); 
        };

        closeChatBtn.addEventListener("click", () => {
            chatPanel.classList.remove("open");
        });

        // 💡 GỬI TIN NHẮN (FIXED)
        sendMessageBtn.addEventListener('click', async () => {
            const content = messageInput.value.trim();
            const receiverUsername = currentReceiverUsername.value.trim();
            
            if (content === "") return;
            
            if (receiverUsername === "" || receiverUsername === "null" || receiverUsername === '[Lỗi User]') {
                alert("Gửi tin nhắn thất bại: Không xác định người nhận.");
                return;
            }

            try {
                const response = await fetch('/chat/send', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: new URLSearchParams({ 
                        receiverUsername: receiverUsername,
                        content: content
                    })
                });
                
                const result = await response.json();
                if (result.success) {
                    // Hiển thị tin nhắn ngay lập tức sau khi gửi
                    appendMessage(content, true); 
                    messageInput.value = '';
                } else {
                    alert("Gửi tin nhắn thất bại. (API báo lỗi)");
                }
                
            } catch (error) {
                alert("Lỗi kết nối khi gửi tin nhắn.");
            }
        });
        
        messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                sendMessageBtn.click();
            }
        });
    }
});