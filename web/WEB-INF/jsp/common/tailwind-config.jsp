<%-- Shared Tailwind CSS configurations for MetaFruit --%>
<script src="${pageContext.request.contextPath}/assets/js/tailwind.js"></script>
<script>
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    /* Brand */
                    "primary":               "#4d661c",
                    "primary-hover":         "#364e03",
                    "primary-light":         "#d9f99d",
                    "primary-dark":          "#364e03",
                    "primary-dk":            "#364e03",
                    "primary-lt":            "#f0f7e6",
                    "primary-fixed":         "#ceee93",
                    "primary-fixed-dim":     "#b3d17a",
                    "primary-container":     "#d9f99d",
                    "on-primary":            "#ffffff",
                    "on-primary-container":  "#597428",
                    "on-primary-fixed":      "#131f00",
                    "on-primary-fixed-variant": "#364e03",
                    "secondary":             "#31694b",
                    "secondary-container":   "#b4f0c9",
                    "secondary-fixed":       "#b4f0c9",
                    "secondary-fixed-dim":   "#99d4ae",
                    "on-secondary":          "#ffffff",
                    "on-secondary-container":"#386f50",
                    "on-secondary-fixed":    "#002111",
                    "on-secondary-fixed-variant": "#175034",
                    "tertiary":              "#486554",
                    "tertiary-container":    "#d5f5e0",
                    "tertiary-fixed":        "#caead6",
                    "tertiary-fixed-dim":    "#afceba",
                    "on-tertiary":           "#ffffff",
                    "on-tertiary-container": "#557161",
                    "on-tertiary-fixed":     "#042014",
                    "on-tertiary-fixed-variant": "#314d3e",
                    /* Surfaces */
                    "background":            "#eaffea",
                    "surface":               "#eaffea",
                    "surface-bright":        "#eaffea",
                    "surface-dim":           "#a9e9b6",
                    "surface-variant":       "#b1f2be",
                    "surface-container":     "#bcfdc9",
                    "surface-container-lowest": "#ffffff",
                    "surface-container-low": "#d1ffd8",
                    "surface-container-high": "#b7f7c3",
                    "surface-container-highest": "#b1f2be",
                    "inverse-surface":       "#00391a",
                    "inverse-on-surface":    "#c3ffce",
                    "inverse-primary":       "#b3d17a",
                    /* Text */
                    "on-background":         "#00210d",
                    "on-surface":            "#00210d",
                    "on-surface-variant":    "#44483b",
                    "on-error":              "#ffffff",
                    "on-error-container":    "#93000a",
                    /* Outline */
                    "outline":               "#75796a",
                    "outline-variant":       "#c5c8b7",
                    /* Semantic */
                    "error":                 "#ba1a1a",
                    "error-container":       "#ffdad6",
                    /* Neutral helpers used by existing JSPs */
                    "surface-2":             "#f8fafc",
                    "txt":                   "#0f172a",
                    "txt-2":                 "#475569",
                    "txt-3":                 "#94a3b8"
                },
                fontFamily: {
                    sans: ["Lexend", "Segoe UI", "sans-serif"]
                },
                spacing: {
                    "xs": "4px",
                    "sm": "12px",
                    "md": "24px",
                    "lg": "40px",
                    "xl": "64px",
                    "gutter": "24px",
                    "margin-mobile": "16px",
                    "margin-desktop": "48px"
                },
                borderRadius: {
                    "2xl": "1rem",
                    "3xl": "1.5rem",
                    "4xl": "2rem",
                    "pill": "9999px"
                },
                boxShadow: {
                    "card": "0 1px 3px rgba(0,0,0,.06),0 4px 16px -4px rgba(20,83,45,.06)"
                },
                transitionTimingFunction: {
                    "out-quart": "cubic-bezier(0.25, 1, 0.5, 1)",
                    "out-quint": "cubic-bezier(0.22, 1, 0.36, 1)",
                    "out-expo": "cubic-bezier(0.16, 1, 0.3, 1)"
                },
                transitionDuration: {
                    "150": "150ms",
                    "250": "250ms",
                    "350": "350ms",
                    "500": "500ms"
                },
                keyframes: {
                    shimmer: {
                        "100%": { transform: "translateX(100%)" }
                    },
                    fadeInUp: {
                        "0%": { opacity: "0", transform: "translateY(10px)" },
                        "100%": { opacity: "1", transform: "translateY(0)" }
                    }
                },
                animation: {
                    "shimmer": "shimmer 2.2s infinite linear",
                    "fade-in-up": "fadeInUp 0.35s cubic-bezier(0.22, 1, 0.36, 1) forwards"
                }
            }
        }
    }
</script>
