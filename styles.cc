* {
  box-sizing: border-box;
}

:root {
  --bg: #060914;
  --bg-panel: rgba(16, 21, 39, 0.9);
  --text: #f4f7ff;
  --muted: #aab7d4;
  --line: rgba(255, 255, 255, 0.08);
  --primary: #ff4d5a;
  --secondary: #ff8b5c;
  --cyan: #58d2ff;
  --shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
}

html { scroll-behavior: smooth; }
body {
  margin: 0;
  font-family: Inter, Arial, sans-serif;
  background:
    radial-gradient(circle at top right, rgba(255, 77, 90, 0.18), transparent 30%),
    radial-gradient(circle at top left, rgba(88, 210, 255, 0.12), transparent 24%),
    var(--bg);
  color: var(--text);
}

a { color: inherit; text-decoration: none; }
img { max-width: 100%; display: block; }

.container {
  width: min(1160px, calc(100% - 32px));
  margin: 0 auto;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(6, 9, 20, 0.75);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);
}

.nav-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 76px;
  gap: 20px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  color: white;
  font-size: 0.7rem;
  box-shadow: 0 10px 25px rgba(255, 77, 90, 0.4);
}

.nav {
  display: flex;
  align-items: center;
  gap: 26px;
  color: var(--muted);
  font-size: 0.95rem;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 46px;
  padding: 0 20px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-weight: 700;
  transition: transform 0.2s ease;
}

.btn:hover { transform: translateY(-1px); }

.btn-primary {
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  box-shadow: 0 15px 30px rgba(255, 77, 90, 0.28);
}

.btn-secondary,
.btn-ghost {
  background: rgba(255,255,255,0.02);
  border-color: var(--line);
}

.hero {
  padding: 74px 0 52px;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 48px;
  align-items: center;
}

.eyebrow {
  display: inline-block;
  background: rgba(88, 210, 255, 0.08);
  border: 1px solid rgba(88, 210, 255, 0.18);
  color: var(--cyan);
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-weight: 700;
}

.hero h1 {
  font-size: clamp(3rem, 7vw, 6.4rem);
  line-height: 0.9;
  letter-spacing: -0.08em;
  margin: 18px 0;
}

.hero h1 span { color: var(--primary); }

.lead {
  max-width: 560px;
  color: var(--muted);
  font-size: 1.08rem;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.stats {
  list-style: none;
  margin: 36px 0 0;
  padding: 0;
  display: flex;
  gap: 26px;
  flex-wrap: wrap;
}

.stats strong {
  display: block;
  font-size: clamp(1.3rem, 2vw, 2rem);
  margin-bottom: 4px;
}

.stats span {
  color: var(--muted);
  font-size: 0.8rem;
}

.hero-visual {
  position: relative;
  min-height: 440px;
  border-radius: 28px;
  border: 1px solid var(--line);
  background: linear-gradient(135deg, rgba(255, 77, 90, 0.15), rgba(88, 210, 255, 0.08)), rgba(11, 16, 28, 0.9);
  overflow: hidden;
  box-shadow: var(--shadow);
}

.hero-visual::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.3), transparent 44%);
}

.car-glow {
  position: absolute;
  inset: 12% 10% 12% 14%;
  border-radius: 24px;
  background: linear-gradient(125deg, rgba(255,77,90,0.18), rgba(88,210,255,0.14));
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.06), 0 0 35px rgba(255,77,90,0.12);
}

.car-glow::before,
.car-glow::after {
  content: "";
  position: absolute;
  border-radius: 20px;
}

.car-glow::before {
  inset: 20% 10% 25% 12%;
  background: linear-gradient(180deg, rgba(255,255,255,0.2), transparent 70%);
  border: 1px solid rgba(255,255,255,0.1);
}

.car-glow::after {
  left: 50%;
  bottom: 18%;
  width: 48%;
  height: 18%;
  transform: translateX(-50%);
  background: linear-gradient(90deg, rgba(0,0,0,0), rgba(255,255,255,0.08), rgba(0,0,0,0));
}

.hud-card {
  position: absolute;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(7, 11, 20, 0.7);
  border: 1px solid var(--line);
  backdrop-filter: blur(6px);
}

.hud-card span {
  display: block;
  color: var(--muted);
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hud-card strong {
  font-size: 1.1rem;
}

.hud-top { top: 38px; left: 32px; }
.hud-bottom { right: 30px; bottom: 36px; }

.panel, .features, .cta { padding: 40px 0 44px; }

.intro-grid {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 26px;
  padding: 30px 22px;
  border: 1px solid var(--line);
  border-radius: 24px;
  background: rgba(17, 23, 38, 0.72);
}

.section-head { margin-bottom: 26px; }
.section-head h2, .intro-grid h2, .cta-box h2 {
  margin: 10px 0 0;
  font-size: clamp(2rem, 4vw, 3rem);
  line-height: 1.08;
  letter-spacing: -0.06em;
}

.intro-grid p:last-child {
  color: var(--muted);
  margin: 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px;
}

.feature-card {
  background: rgba(16, 21, 39, 0.82);
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 24px;
  box-shadow: var(--shadow);
}

.feature-badge {
  display: inline-grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(255,77,90,0.18), rgba(88,210,255,0.12));
  border: 1px solid rgba(255,255,255,0.06);
  font-weight: 800;
  margin-bottom: 18px;
}

.feature-card h3 { margin: 0 0 10px; font-size: 1.3rem; }
.feature-card p, .site-footer p { color: var(--muted); }

.cta-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 32px 26px;
  background: linear-gradient(135deg, rgba(255,77,90,0.12), rgba(88,210,255,0.08));
  border: 1px solid var(--line);
  border-radius: 28px;
}

.cta-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.site-footer {
  border-top: 1px solid var(--line);
  padding: 24px 0 36px;
}

.footer-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.footer-brand {
  margin-bottom: 10px;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  color: var(--muted);
}

.copyright {
  border-top: 1px solid var(--line);
  padding-top: 18px;
  color: var(--muted);
  text-align: center;
}

.legal-page {
  padding: 72px 0 88px;
}

.legal-shell {
  max-width: 980px;
  margin: 0 auto;
  background: rgba(17, 21, 34, 0.9);
  border: 1px solid var(--line);
  border-radius: 26px;
  padding: clamp(24px, 4vw, 42px);
  box-shadow: var(--shadow);
}

.legal-shell h1 {
  margin: 12px 0 8px;
  font-size: clamp(2.1rem, 4vw, 3.4rem);
  letter-spacing: -0.06em;
}

.legal-shell h2 { margin-top: 28px; }
.legal-shell h3 { margin-top: 18px; }
.legal-shell p, .legal-shell li { color: var(--muted); }
.legal-shell a { color: var(--cyan); }

.back-link {
  display: inline-flex;
  color: var(--muted);
}

@media (max-width: 860px) {
  .hero-grid, .intro-grid, .card-grid, .cta-box, .footer-wrap {
    grid-template-columns: 1fr;
    display: grid;
  }
  .nav { display: none; }
  .nav-wrap { justify-content: space-between; }
}

@media (max-width: 560px) {
  .hero-actions, .cta-actions, .nav-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .btn { width: 100%; }
  .stats {
    display: grid;
    grid-template-columns: 1fr;
    gap: 14px;
  }
}
