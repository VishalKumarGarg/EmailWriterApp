console.log("Email Writer Extension content script loaded.");
function getEmailContent() {
    let body = '';

    // Get the email body
    const bodyEl = document.querySelector('.a3s.aiL'); // Gmail email body
    if (bodyEl) {
        // Clone to avoid modifying the original DOM
        const clonedBody = bodyEl.cloneNode(true);

        // Remove unwanted elements
        const unwantedSelectors = ['.gmail_quote', 'div[role="button"]', '.adC'];
        unwantedSelectors.forEach(sel => {
            clonedBody.querySelectorAll(sel).forEach(el => el.remove());
        });

        // Get only the text
        body = clonedBody.innerText.trim();
    }

    return body;
}


function findComposeToolbar() {
    const selectors = ['.btC', '.aDh', '[role="toolbar"]', '.gU.Up']
    for (const selector of selectors) {
        const toolbar = document.querySelector(selector);
        if (toolbar)
            return toolbar;
    }
    return null;

}
function createAIButton() {
    const button = document.createElement('div');
    button.className = 'T-I J-J5-Ji aoO v7 T-I-atl L3 ai-reply-button';
    button.style.marginRight = '8px';
    button.innerText = 'AI Reply';
    button.setAttribute('role', 'button');
    button.setAttribute('data-tooltip', 'Generate AI Reply');
    return button;
}
function injectButton() {
    const existingButton = document.querySelector('.ai-reply-button');
    if (existingButton) {
        existingButton.remove();
    }
    const toolbar = findComposeToolbar();
    if (!toolbar) {
        console.log("Compose toolbar not found.");
        return;
    }
    console.log("Toolbar found: ", toolbar);

    const button = createAIButton();

    button.addEventListener('click', async () => {
        // *** SHOW TONE SELECTION POPUP ***
        const wrapper = document.createElement('div');
        wrapper.style.position = 'fixed';
        wrapper.style.top = '50%';
        wrapper.style.left = '50%';
        wrapper.style.transform = 'translate(-50%, -50%)';
        wrapper.style.background = 'white';
        wrapper.style.border = '1px solid #ccc';
        wrapper.style.padding = '15px';
        wrapper.style.borderRadius = '8px';
        wrapper.style.zIndex = '99999';
        wrapper.style.boxShadow = '0 4px 10px rgba(0,0,0,0.15)';
        wrapper.innerHTML = `
            <h3 style="margin-top:0;font-size:16px;">Choose Tone</h3>
            <button class="tone-btn" data-tone="professional" style="margin-right:10px;">Professional</button>
            <button class="tone-btn" data-tone="casual" style="margin-right:10px;">Casual</button>
            <button class="tone-btn" data-tone="friendly">Friendly</button>
        `;
        document.body.appendChild(wrapper);

        // When the user clicks a tone
        wrapper.querySelectorAll('.tone-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const selectedTone = btn.getAttribute('data-tone');
                wrapper.remove(); // remove popup

                try {
                    button.innerText = 'Generating...';
                    button.disabled = true;

                    const emailContent = getEmailContent();
                    const response = await fetch('http://localhost:8089/api/email/generator2', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            emailContent: emailContent,
                            tone: selectedTone
                        })
                    });

                    if (!response.ok) throw new Error("API Request Failed");

                    const genReply = await response.text();
                    const composeBox = document.querySelector('[role="textbox"][g_editable="true"]');

                    if (composeBox) {
                        composeBox.focus();

                        // Reset cursor to top
                        window.getSelection().removeAllRanges();

                        // Clear old content
                        composeBox.innerHTML = '';

                        // Insert new reply (supports line breaks)
                        composeBox.innerHTML = genReply.replace(/\n/g, "<br>");
                    }


                } catch (error) {
                    console.error("Error generating reply:", error);
                } finally {
                    button.innerText = 'AI Reply';
                    button.disabled = false;
                }
            });
        });
    });

    toolbar.insertBefore(button, toolbar.firstChild);
}


const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
        const addedNodes = Array.from(mutation.addedNodes);
        const hasComposeElement = addedNodes.some(node =>
            node.nodeType === Node.ELEMENT_NODE &&
            (node.matches('.aDh, .btC, [role="dialog"]')
                || node.querySelector('.aDh, .btC, [role="dialog"]'))
        );
        if (hasComposeElement) {
            console.log("Compose window Detected. ");
            setTimeout(injectButton, 800); // Delay to ensure the compose window is fully loaded
        }
    }
});
observer.observe(document.body, { childList: true, subtree: true });