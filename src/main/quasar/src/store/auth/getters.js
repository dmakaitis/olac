export function isLoggedIn(state) {
  if (state.jwtToken) {
    return true;
  } else {
    return false;
  }
}

export function authHeader(state) {
  return `Bearer ${state.jwtToken}`
}

export function isAdmin(state) {
  return !state.grants.includes('ROLE_ADMIN')
}
