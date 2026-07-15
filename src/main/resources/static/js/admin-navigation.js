(function () {
    const path = window.location.pathname;

    const contextMatch = path.match(/^(.*)\/admin(?:\/|$)/);
    const contextPath = contextMatch ? contextMatch[1] : "";
    const withContext = (url) => `${contextPath}${url}`;

    const nav = [
        {
            label: "상점 관리",
            icon: "store",
            base: ["/admin/shop"],
            links: [
                { label: "상점목록", url: "/admin/shop/list" },
                { label: "매출현황", url: "/admin/shop/sales" }
            ]
        },
        {
            label: "회원 관리",
            icon: "group",
            base: ["/admin/member"],
            links: [
                { label: "회원목록", url: "/admin/member/list" },
                { label: "포인트관리", url: "/admin/member/point" }
            ]
        },
        {
            label: "상품 관리",
            icon: "inventory_2",
            base: ["/admin/product"],
            links: [
                { label: "상품목록", url: "/admin/product/list" },
                { label: "상품등록", url: "/admin/product/register" }
            ]
        },
        {
            label: "주문 관리",
            icon: "shopping_cart",
            base: ["/admin/order"],
            links: [
                { label: "주문현황", url: "/admin/order/list" },
                { label: "배송현황", url: "/admin/order/delivery" }
            ]
        },
        {
            label: "쿠폰 관리",
            icon: "confirmation_number",
            base: ["/admin/coupon"],
            links: [
                { label: "쿠폰목록", url: "/admin/coupon/list" },
                { label: "쿠폰발급현황", url: "/admin/coupon/issued" }
            ]
        },
        {
            label: "고객 센터",
            icon: "support_agent",
            base: ["/admin/cs"],
            links: [
                { label: "공지사항", url: "/admin/cs/notice/list" },
                { label: "자주묻는질문", url: "/admin/cs/faq/list" },
                { label: "문의하기", url: "/admin/cs/qna/list" }
            ]
        },
        {
            label: "환경 설정",
            icon: "settings",
            base: ["/admin/config"],
            links: [
                { label: "기본설정", url: "/admin/config/basic" },
                { label: "배너관리", url: "/admin/config/banner" },
                { label: "약관관리", url: "/admin/config/policy" },
                { label: "카테고리", url: "/admin/config/category" },
                { label: "버전관리", url: "/admin/config/version" }
            ]
        }
    ];

    const ensureIconFont = () => {
        const href = "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap";
        const hasIconFont = Array.from(document.querySelectorAll("link[href]"))
            .some((link) => link.href.includes("Material+Symbols+Outlined"));

        if (!hasIconFont) {
            const link = document.createElement("link");
            link.rel = "stylesheet";
            link.href = href;
            document.head.appendChild(link);
        }
    };

    const ensureCommonStyles = () => {
        if (document.getElementById("admin-navigation-style")) {
            return;
        }

        const style = document.createElement("style");
        style.id = "admin-navigation-style";
        style.textContent = `
            .material-symbols-outlined { font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24; }
            .menu-content { max-height: 0; overflow: hidden; opacity: 0; transition: max-height .3s ease-in-out, opacity .2s ease-in-out, padding .3s ease-in-out; }
            .menu-item-active .menu-content { max-height: 500px; opacity: 1; padding-top: 4px; padding-bottom: 8px; }
            .chevron-icon { transition: transform .3s ease; }
            .menu-item-active .chevron-icon { transform: rotate(180deg); }
            .bg-background, .bg-surface { background-color: #f8f9fa; }
            .bg-surface-container-lowest { background-color: #ffffff; }
            .bg-surface-container-low { background-color: #f3f4f5; }
            .bg-surface-container-high { background-color: #e7e8e9; }
            .bg-surface-container { background-color: #edeeef; }
            .text-primary { color: #14333b; }
            .text-on-background, .text-on-surface { color: #191c1d; }
            .text-on-surface-variant { color: #41484a; }
            .border-outline-variant { border-color: #c1c7ca; }
            .px-md { padding-left: 24px; padding-right: 24px; }
            .py-lg { padding-top: 48px; padding-bottom: 48px; }
            .py-md { padding-top: 24px; padding-bottom: 24px; }
            .p-lg { padding: 48px; }
            .p-base { padding: 8px; }
            .gap-md { gap: 24px; }
            .gap-xs { gap: 4px; }
            .mt-sm { margin-top: 12px; }
            .font-headline-md { font-family: "Plus Jakarta Sans", sans-serif; }
            .font-body-md, .font-label-md { font-family: "Be Vietnam Pro", sans-serif; }
            .text-headline-md { font-size: 20px; line-height: 1.4; }
            .text-label-md { font-size: 14px; line-height: 1.4; }
            .hover\\:bg-surface-container-low:hover { background-color: #f3f4f5; }
            .hover\\:bg-surface-container-high:hover { background-color: #e7e8e9; }
            .hover\\:text-primary:hover { color: #14333b; }
            .hover\\:text-on-surface:hover { color: #191c1d; }
        `;
        document.head.appendChild(style);
    };

    const isActiveUrl = (url) => {
        const full = withContext(url);
        return path === full || path.startsWith(`${full}/`);
    };

    const isActiveGroup = (item) => item.base.some((base) => path.startsWith(withContext(base)));

    const renderSidebar = () => {
        let aside = document.querySelector("aside");
        if (!aside) {
            aside = document.createElement("aside");
            document.body.prepend(aside);
        }

        aside.className = "fixed left-0 top-0 bottom-0 flex flex-col w-64 z-40 bg-surface border-r border-outline-variant overflow-hidden";
        aside.innerHTML = `
            <a class="flex items-center gap-md px-md py-lg border-b border-outline-variant flex-shrink-0 hover:bg-surface-container-low transition-colors" href="${withContext("/admin/index")}" aria-label="관리자 메인으로 이동">
                <div class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center text-primary">
                    <span class="material-symbols-outlined">storefront</span>
                </div>
                <div>
                    <h2 class="font-headline-md text-headline-md font-bold text-primary">K-market<br>Admin</h2>
                    <p class="font-label-md text-label-md text-on-surface-variant">관리자 모드</p>
                </div>
            </a>
            <div class="flex-1 overflow-y-auto py-md px-base flex flex-col gap-xs" data-admin-menu></div>
            <div class="border-t border-outline-variant p-base flex flex-col gap-xs flex-shrink-0">
                <a class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container-high rounded-lg transition-colors" href="${withContext("/admin/index")}">
                    <span class="material-symbols-outlined">history</span>
                    <span class="font-label-md text-label-md">시스템 로그</span>
                </a>
                <a class="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container-high rounded-lg transition-colors" href="${withContext("/admin/index")}">
                    <span class="material-symbols-outlined">help</span>
                    <span class="font-label-md text-label-md">도움말</span>
                </a>
                <a class="mt-sm flex items-center justify-center gap-2 w-full py-2 border border-outline-variant rounded-lg text-primary hover:bg-surface-container-high transition-colors font-label-md text-label-md" href="${withContext("/member/logout")}">
                    <span class="material-symbols-outlined text-sm">logout</span>
                    로그아웃
                </a>
            </div>
        `;

        const menu = aside.querySelector("[data-admin-menu]");
        nav.forEach((item) => {
            const groupActive = isActiveGroup(item);
            const group = document.createElement("div");
            group.className = `group/menu${groupActive ? " menu-item-active" : ""}`;

            const buttonClass = groupActive
                ? "w-full flex items-center justify-between px-4 py-3 text-primary bg-surface-container-high rounded-lg transition-colors group"
                : "w-full flex items-center justify-between px-4 py-3 text-on-surface-variant hover:bg-surface-container-high rounded-lg transition-colors group";

            group.innerHTML = `
                <button class="${buttonClass}" type="button" onclick="toggleAccordion(this)">
                    <div class="flex items-center gap-3">
                        <span class="material-symbols-outlined"${groupActive ? " style=\"font-variation-settings: 'FILL' 1;\"" : ""}>${item.icon}</span>
                        <span class="font-label-md text-label-md${groupActive ? " font-bold" : ""}">${item.label}</span>
                    </div>
                    <span class="material-symbols-outlined text-sm chevron-icon">expand_more</span>
                </button>
                <div class="menu-content flex flex-col gap-1 pl-12 pr-4"></div>
            `;

            const linkBox = group.querySelector(".menu-content");
            item.links.forEach((link) => {
                const a = document.createElement("a");
                a.href = withContext(link.url);
                a.textContent = link.label;
                a.className = isActiveUrl(link.url)
                    ? "py-2 text-primary font-bold font-label-md transition-colors"
                    : "py-2 text-on-surface-variant hover:text-primary font-label-md transition-colors";
                linkBox.appendChild(a);
            });

            menu.appendChild(group);
        });
    };

    const renderHeader = () => {
        let header = document.querySelector("header");
        if (!header) {
            header = document.createElement("header");
            const aside = document.querySelector("aside");
            aside ? aside.after(header) : document.body.prepend(header);
        }

        header.className = "fixed top-0 right-0 left-64 h-16 flex items-center justify-between px-md z-30 bg-surface-container-lowest border-b border-outline-variant";
        header.innerHTML = `
            <a class="font-headline-md text-headline-md font-black text-primary hover:text-on-surface transition-colors truncate" href="${withContext("/admin/index")}" aria-label="관리자 메인으로 이동">K-market Admin Dashboard</a>
            <nav class="ml-auto flex items-center gap-2 text-sm font-label-md" data-admin-header-links="true">
                <a class="px-3 py-2 rounded border border-outline-variant text-primary hover:bg-surface-container-high transition-colors whitespace-nowrap" href="${withContext("/admin/index")}">관리자 메인</a>
                <a class="px-3 py-2 rounded border border-outline-variant text-primary hover:bg-surface-container-high transition-colors whitespace-nowrap" href="${contextPath ? `${contextPath}/` : "/"}">메인페이지</a>
            </nav>
        `;
    };

    const normalizeLayout = () => {
        document.body.classList.remove("p-8", "p-lg", "bg-[#f8f9fa]", "font-sans");
        document.body.classList.add("bg-background", "text-on-background", "font-body-md", "text-body-md", "antialiased", "min-h-screen", "flex");

        let main = document.querySelector("main");
        if (!main) {
            main = document.createElement("main");
            const movable = Array.from(document.body.childNodes).filter((node) => {
                if (node.nodeType === Node.TEXT_NODE) {
                    return node.textContent.trim().length > 0;
                }
                if (node.nodeType !== Node.ELEMENT_NODE) {
                    return false;
                }
                return !["ASIDE", "HEADER", "SCRIPT"].includes(node.tagName);
            });

            movable.forEach((node) => main.appendChild(node));
            const firstScript = document.body.querySelector("script");
            firstScript ? document.body.insertBefore(main, firstScript) : document.body.appendChild(main);
        }

        main.classList.add("ml-64", "mt-16", "p-lg", "flex-1", "w-full", "max-w-[1400px]", "mx-auto");

        const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        if (!reduceMotion) {
            main.style.opacity = "0";
            main.style.transform = "translateY(8px)";
            main.style.transition = "opacity 220ms ease-out, transform 220ms ease-out";

            window.requestAnimationFrame(() => {
                main.style.opacity = "1";
                main.style.transform = "translateY(0)";
            });
        }
    };

    window.toggleAccordion = function (button) {
        const menuItem = button.closest(".group\\/menu");
        if (!menuItem) return;

        const isActive = menuItem.classList.contains("menu-item-active");
        if (isActive) {
            menuItem.classList.remove("menu-item-active");
            return;
        }

        document.querySelectorAll(".group\\/menu.menu-item-active").forEach((item) => {
            item.classList.remove("menu-item-active");
        });
        menuItem.classList.add("menu-item-active");
    };

    ensureIconFont();
    ensureCommonStyles();
    renderSidebar();
    renderHeader();
    normalizeLayout();
})();
