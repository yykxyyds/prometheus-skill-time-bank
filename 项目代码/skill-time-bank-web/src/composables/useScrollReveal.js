import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'

/**
 * 滚动入场动画 composable
 * 当元素进入视口时触发淡入 + 上移动画
 * @param {string} selector - CSS 选择器
 * @param {Object} options
 * @param {number} options.threshold - 触发比例，默认 0.1
 * @param {number} options.stagger - 逐条延迟(ms)，默认 60
 * @param {string} options.rootMargin - 视口边距，默认 '0px 0px -40px 0px'
 */
export function useScrollReveal(selector, options = {}) {
  const { threshold = 0.1, stagger = 60, rootMargin = '0px 0px -40px 0px' } = options
  const revealed = ref(false)

  let observer = null
  let mutationObserver = null

  function observeElements() {
    const elements = document.querySelectorAll(selector)
    if (!elements.length) return

    if (!observer) {
      observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              const allElements = [...document.querySelectorAll(selector)]
              const idx = allElements.indexOf(entry.target)
              const delay = idx >= 0 ? idx * stagger : 0

              setTimeout(() => {
                entry.target.classList.add('revealed')
              }, delay)

              observer.unobserve(entry.target)
            }
          })
        },
        { threshold, rootMargin }
      )
    }

    elements.forEach((el) => {
      if (!el.classList.contains('revealed')) {
        observer.observe(el)
      }
    })
    revealed.value = true
  }

  onMounted(() => {
    // 首次尝试观察
    nextTick(() => observeElements())

    // 使用 MutationObserver 监听 DOM 变化（处理异步加载的数据）
    mutationObserver = new MutationObserver(() => {
      observeElements()
    })
    mutationObserver.observe(document.body, {
      childList: true,
      subtree: true
    })
  })

  onBeforeUnmount(() => {
    if (observer) observer.disconnect()
    if (mutationObserver) mutationObserver.disconnect()
  })

  return { revealed }
}
