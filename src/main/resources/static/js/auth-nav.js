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
        if (!anchor) {
            return false;
        }
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

    function makeLink(reference, text, path, options = {}) {
        const link = document.createElement('a');
        link.className = reference.className;
        link.href = appUrl(path);
        link.title = options.title || text;

        if (options.icon) {
            link.classList.add('flex', 'items-center', 'gap-1');
            const icon = document.createElement('span');
            icon.className = 'material-symbols-outlined text-sm';
            icon.textContent = options.icon;
            link.append(icon, document.createTextNode(text));
        } else {
            link.textContent = text;
        }

        if (options.admin) {
            link.classList.add('font-bold', 'border-b-2', 'pb-1');
            link.style.color = '#ba1a1a';
            link.style.borderBottomColor = '#ba1a1a';
        }

        return link;
    }

    function insertBeforeService(container, link) {
        const serviceLink = Array.from(container.querySelectorAll('a'))
            .find(existing => matchesPath(existing, '/cs/index'));
        if (serviceLink && !matchesPath(link, '/cs/index')) {
            serviceLink.insertAdjacentElement('beforebegin', link);
        } else {
            container.appendChild(link);
        }
    }

    function ensureLink(container, reference, text, path, options = {}) {
        const links = Array.from(container.querySelectorAll('a'));
        const found = links.find(link => matchesPath(link, path));
        if (found) {
            if (options.admin) {
                found.style.color = '#ba1a1a';
                found.style.borderBottomColor = '#ba1a1a';
                found.classList.add('font-bold', 'border-b-2', 'pb-1');
            }
            return found;
        }

        const link = makeLink(reference, text, path, options);
        insertBeforeService(container, link);
        return link;
    }

    function updateNavigation(session) {
        const username = session.username || '';
        const role = (session.role || '').toUpperCase();
        const isAdmin = session.admin === true || role === 'ADMIN';

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
                ensureLink(container, loginLink, '로그아웃', '/member/logout', { title: '로그아웃' });
            }

            if (isAdmin) {
                ensureLink(container, loginLink, '관리자', '/admin/index', {
                    admin: true,
                    title: '관리자 페이지로 이동'
                });
            }

            if (!myPageLink) {
                const newMyPageLink = makeLink(loginLink, '마이페이지', '/my/index', { title: '마이페이지로 이동' });
                insertBeforeService(container, newMyPageLink);
            } else {
                myPageLink.href = appUrl('/my/index');
            }

            ensureLink(container, loginLink, '고객센터', '/cs/index', {
                icon: 'support_agent',
                title: '고객센터로 이동'
            });
        });
    }

    fetch(appUrl('/member/session'), { credentials: 'same-origin' })
        .then(response => response.ok ? response.json() : null)
        .then(session => {
            if (session && session.authenticated) {
                updateNavigation(session);
            }
        })
        .catch(() => {});
})();