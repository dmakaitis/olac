export function storeConfig(state, config) {
  state.config = config || {};
}

export function setShowLogin(state, showLogin) {
  state.config = state.config || {}
  state.config.showLogin = showLogin
}
