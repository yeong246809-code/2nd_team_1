(function() {
    const currentScript = document.currentScript || document.querySelector('script[src*="/js/auth-nav.js"]');

    function contextPath() {
        if (!currentScript) {
            return '';
        }
        const scriptUrl = new URL(currentScript.getAttribute('src'), window.location.href);
        const marker = '/js/auth-nav.js';
        const index = scriptUrl.pathname.indexOf(marker);
        return index > 0 ? scriptUrl.pathname.substring(0, index) : '';
    }

    const context = contextPath();

    function appUrl(path) {
        return `${context}${path}`;
    }

    function matchesPath(anchor, path) {
        const rawHref = anchor.getAttribute('href') || '';
        if (rawHref === path || rawHref === appUrl(path) || rawHref.endsWith(path)) {
            return true;
        }

        try {
            return new URL(anchor.href, window.location.href).pathname.endsWith(path);
        } catch (error) {
            return false;
        }
    }

    function makeLink(reference, text, path) {
        const link = document.createElement('a');
        link.className = reference.className;
        link.href = appUrl(path);
        link.textContent = text;
        return link;
    }

    function updateNavigation(username) {
        const loginLinks = Array.from(document.querySelectorAll('a'))
            .filter(link => matchesPath(link, '/member/login'));

        loginLinks.forEach(loginLink => {
            const container = loginLink.parentElement || document.body;
            const links = Array.from(container.querySelectorAll('a'));
            const joinLink = links.find(link => matchesPath(link, '/member/join'));
            const myPageLink = links.find(link => matchesPath(link, '/my/index'));

            loginLink.href = appUrl('/my/index');
            loginLink.textContent = `${username}님`;
            loginLink.title = '마이페이지로 이동';

            if (joinLink) {
                joinLink.href = appUrl('/member/logout');
                joinLink.textContent = '로그아웃';
                joinLink.title = '로그아웃';
            } else if (!links.find(link => matchesPath(link, '/member/logout'))) {
                loginLink.insertAdjacentElement('afterend', makeLink(loginLink, '로그아웃', '/member/logout'));
            }

            if (!myPageLink) {
                loginLink.insertAdjacentElement('afterend', makeLink(loginLink, '마이페이지', '/my/index'));
            } else {
                myPageLink.href = appUrl('/my/index');
            }
        });
    }

    fetch(appUrl('/member/session'), { credentials: 'same-origin' })
        .then(response => response.ok ? response.json() : null)
        .then(session => {
            if (session && session.authenticated) {
                updateNavigation(session.username);
            }
        })
        .catch(() => {});
})();
