export function showLogin(state) {
  console.log(`Show login: ${state.config.showLogin}`)
  return state.config.showLogin
}

export function enableReservations(state) {
  console.log(`Enable reservations: ${state.config.enableReservations}`)
  return state.config.enableReservations
}
