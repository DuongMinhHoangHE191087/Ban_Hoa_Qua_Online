---
name: Verdant Clarity
colors:
  surface: '#eaffea'
  surface-dim: '#a9e9b6'
  surface-bright: '#eaffea'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#d1ffd8'
  surface-container: '#bcfdc9'
  surface-container-high: '#b7f7c3'
  surface-container-highest: '#b1f2be'
  on-surface: '#00210d'
  on-surface-variant: '#44483b'
  inverse-surface: '#00391a'
  inverse-on-surface: '#c3ffce'
  outline: '#75796a'
  outline-variant: '#c5c8b7'
  surface-tint: '#4d661c'
  primary: '#4d661c'
  on-primary: '#ffffff'
  primary-container: '#d9f99d'
  on-primary-container: '#597428'
  inverse-primary: '#b3d17a'
  secondary: '#31694b'
  on-secondary: '#ffffff'
  secondary-container: '#b4f0c9'
  on-secondary-container: '#386f50'
  tertiary: '#486554'
  on-tertiary: '#ffffff'
  tertiary-container: '#d5f5e0'
  on-tertiary-container: '#557161'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ceee93'
  primary-fixed-dim: '#b3d17a'
  on-primary-fixed: '#131f00'
  on-primary-fixed-variant: '#364e03'
  secondary-fixed: '#b4f0c9'
  secondary-fixed-dim: '#99d4ae'
  on-secondary-fixed: '#002111'
  on-secondary-fixed-variant: '#175034'
  tertiary-fixed: '#caead6'
  tertiary-fixed-dim: '#afceba'
  on-tertiary-fixed: '#042014'
  on-tertiary-fixed-variant: '#314d3e'
  background: '#eaffea'
  on-background: '#00210d'
  surface-variant: '#b1f2be'
typography:
  display-lg:
    fontFamily: Lexend
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Lexend
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Lexend
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  headline-md:
    fontFamily: Lexend
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Lexend
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Lexend
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Lexend
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-sm:
    fontFamily: Lexend
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 40px
  xl: 64px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: 48px
---

## Brand & Style
The design system is centered on the concept of "Verdant Clarity"—merging the organic vitality of fresh produce with the precision of modern inventory management. The brand personality is professional yet approachable, evoking feelings of growth, health, and systematic efficiency.

The visual style employs a sophisticated **Glassmorphism** approach. This allows the vibrant green color palette to feel airy rather than heavy. By using translucent layers, frosted glass effects, and soft background blurs, the interface maintains a sense of depth and modernity while prioritizing content legibility. The aesthetic is clean and minimal, emphasizing high-quality typography and generous whitespace to reflect the "freshness" of the product.

## Colors
The palette is inspired by a thriving orchard, utilizing a range of greens to establish hierarchy and mood.

- **Primary:** A bright Lime Green (#D9F99D) used for primary brand moments and highlights.
- **Surface Gradients:** Transitions from Soft Mint (#BBF7D0) to Pale Emerald (#DCFCE7) are used for large background areas or glassmorphic containers to add dimension.
- **Deep Contrast:** Forest Green (#14532D) is the foundational color for text, iconography, and high-emphasis interactive states (hover/active).
- **Background:** Use an ultra-light tinted white (#F7FEE7) for the main application background to keep the "organic" feel consistent even in neutral areas.

## Typography
Lexend is selected as the sole typeface for the design system due to its unique origins in reading proficiency and its friendly, geometric character. It strikes the perfect balance between "organic" and "technical."

- **Headlines:** Use Bold and SemiBold weights in Forest Green to anchor the page.
- **Body Text:** Use Regular weight for high readability in data-heavy management views.
- **Labels:** Use Medium and SemiBold weights for buttons and navigation items to ensure they stand out against glassmorphic backgrounds.
- **Numerical Data:** Lexend’s clear character definition makes it ideal for the dashboard’s charts and management tables.

## Layout & Spacing
This design system utilizes a **fluid grid** model with a base-8 spacing rhythm to ensure visual harmony.

- **Desktop (1440px+):** 12-column grid with 24px gutters and 48px outside margins.
- **Tablet (768px - 1439px):** 8-column grid with 20px gutters and 32px outside margins.
- **Mobile (Up to 767px):** 4-column grid with 16px gutters and 16px outside margins.

Layouts should prioritize "containment within glass." Dashboard widgets and product cards should use dynamic padding (typically `md` or `lg`) to prevent information density from feeling overwhelming.

## Elevation & Depth
Depth is communicated through **Glassmorphism and Ambient Shadows**.

1.  **Base Layer:** Solid off-white/green tinted background.
2.  **Mid Layer (Cards/Widgets):** Semi-transparent white (#FFFFFF at 60-80% opacity) with a `backdrop-filter: blur(12px)`. These elements feature a 1px solid white border at 20% opacity to simulate the edge of a glass pane.
3.  **Top Layer (Modals/Popovers):** Higher opacity white with a more pronounced ambient shadow. Shadows should not be neutral grey; instead, use a highly desaturated Forest Green (#14532D) at 5-10% alpha to maintain the organic warmth.

Avoid heavy, dark shadows. The goal is to make elements feel like they are floating slightly above a lush, green surface.

## Shapes
The shape language is overtly soft and approachable. Following the "2xl" requirement, the design system adopts a large-radius philosophy:

- **Standard Elements (Buttons, Inputs):** 0.5rem (8px) radius.
- **Containers (Cards, Widgets, Sections):** 1.5rem (24px) radius.
- **Large Components (Modals, Hero sections):** 2rem (32px) radius.

All corners should use **continuous curvature** (squircle-like) where possible to enhance the organic, premium feel.

## Components

### Buttons & Interactive
- **Primary Button:** Solid Forest Green (#14532D) with white Lexend text. High contrast is essential here.
- **Secondary Button:** Glassmorphic background with a Forest Green border and text.
- **Tertiary/Ghost:** No background, Forest Green text, underline on hover.

### Forms & Inputs
- **Fields:** Subtle Pale Emerald (#DCFCE7) background with a 1px Forest Green border that thickens on focus.
- **Labels:** Always placed above the field in Forest Green, using `label-md`.

### Data Tables
- **Structure:** No vertical borders. Use thin Soft Mint (#BBF7D0) horizontal separators.
- **Header:** Semi-bold Forest Green text with a very subtle glass tint background for the header row.
- **Row Hover:** Apply a 40% opacity Lime Green highlight on hover.

### Dashboard Widgets & Cards
- **Construction:** Apply the standard glassmorphism stack (blur + white border).
- **Charts:** Use a palette of varying greens (from Lime to Forest) and one contrasting "accent" (like a soft orange) only for critical alerts. Lines should be smooth and rounded.

### Product Cards
- **Visuals:** High-resolution produce imagery should be clipped with a 1.5rem radius.
- **Overlay:** Text overlays for fruit names should use a glassmorphic "tag" at the bottom of the image for maximum legibility.