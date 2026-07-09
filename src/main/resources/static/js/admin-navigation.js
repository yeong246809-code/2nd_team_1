(function () {
    const aside = document.querySelector("aside");
    const path = window.location.pathname;
    const contextMatch = path.match(/^(.*)\/admin(?:\/|$)/);
    const contextPath = contextMatch ? contextMatch[1] : "";
    const withContext = (url) => `${contextPath}${url}`;

    const nav = [
        {
            base: ["/admin/shop", "/admin/index"],
            links: ["/admin/shop/list", "/admin/shop/sales"]
        },
        {
            base: ["/admin/member"],
            links: ["/admin/member/list", "/admin/member/list"]
        },
        {
            base: ["/admin/product"],
            links: ["/admin/product/list", "/admin/product/register"]
        },
        {
            base: ["/admin/order"],
            links: ["/admin/order/list", "/admin/order/list"]
        },
        {
            base: ["/admin/coupon"],
            links: ["/admin/index", "/admin/index"]
        },
        {
            base: ["/cs"],
            links: ["/cs/notice/list", "/cs/faq/list", "/cs/qna/list"]
        },
        {
            base: ["/admin/config"],
            links: [
                "/admin/config/basic",
                "/admin/config/banner",
                "/admin/config/policy",
                "/admin/config/category",
                "/admin/config/version"
            ]
        }
    ];

    const isActiveUrl = (url) => {
        const full = withContext(url);
        return path === full || path.startsWith(`${full}/`);
    };

    const clearActiveLink = (link) => {
        link.classList.remove("text-primary", "font-bold");
        link.classList.add("text-on-surface-variant", "hover:text-primary");
    };

    const setActiveLink = (link) => {
        link.classList.remove("text-on-surface-variant", "hover:text-primary");
        link.classList.add("text-primary", "font-bold");
    };

    const clearGroupTitle = (group) => {
        const title = group.querySelector("button span:last-child");
        if (!title) return;
        title.classList.remove("text-primary", "font-bold");
    };

    const setGroupTitle = (group) => {
        const title = group.querySelector("button span:last-child");
        if (!title) return;
        title.classList.add("text-primary", "font-bold");
    };

    if (aside) {
        const groups = Array.from(aside.querySelectorAll(".group\\/menu"));
        groups.forEach((group, groupIndex) => {
            const config = nav[groupIndex];
            if (!config) return;

            group.classList.remove("menu-item-active");
            clearGroupTitle(group);

            const links = Array.from(group.querySelectorAll(".menu-content a"));
            links.forEach((link, linkIndex) => {
                const target = config.links[linkIndex];
                if (!target) return;
                link.href = withContext(target);
                clearActiveLink(link);
            });

            const groupActive = config.base.some((base) => path.startsWith(withContext(base)));
            if (!groupActive) return;

            group.classList.add("menu-item-active");
            setGroupTitle(group);

            links.forEach((link, linkIndex) => {
                const target = config.links[linkIndex];
                if (target && isActiveUrl(target)) {
                    setActiveLink(link);
                }
            });
        });
    }

    const header = document.querySelector("header");
    if (header) {
        const existingTitle = header.querySelector("h1, h2, a");
        const titleText = existingTitle && existingTitle.textContent.trim()
            ? existingTitle.textContent.trim()
            : "K-market Admin Dashboard";

        header.classList.remove("justify-end");
        header.classList.add("flex", "items-center", "justify-between", "h-16", "px-md");
        header.replaceChildren();

        const titleWrap = document.createElement("div");
        titleWrap.className = "flex items-center gap-md min-w-0";

        const titleLink = document.createElement("a");
        titleLink.href = withContext("/admin/index");
        titleLink.className = "font-headline-md text-headline-md font-black text-primary dark:text-inverse-primary hover:text-on-surface transition-colors truncate";
        titleLink.textContent = titleText;
        titleLink.setAttribute("aria-label", "관리자 메인으로 이동");

        const navLinks = document.createElement("nav");
        navLinks.dataset.adminHeaderLinks = "true";
        navLinks.className = "ml-auto flex items-center gap-2 text-sm font-label-md";
        navLinks.innerHTML = `
            <a class="px-3 py-2 rounded border border-outline-variant text-primary hover:bg-surface-container-high transition-colors whitespace-nowrap" href="${withContext("/admin/index")}">관리자 메인</a>
            <a class="px-3 py-2 rounded border border-outline-variant text-primary hover:bg-surface-container-high transition-colors whitespace-nowrap" href="${contextPath ? `${contextPath}/` : "/"}">메인페이지</a>
        `;

        titleWrap.appendChild(titleLink);
        header.append(titleWrap, navLinks);
    }
})();
