let initialized = false;

export function initRem() {
  if (initialized) return;
  initialized = true;

  const setRem = () => {
    let fontSize = document.documentElement.clientWidth / (15 * 2);
    if (fontSize > 16) {
      fontSize = 16;
    }
    document.documentElement.style.fontSize = `${fontSize}px`;
  };

  setRem();
  window.addEventListener("resize", setRem);
}
