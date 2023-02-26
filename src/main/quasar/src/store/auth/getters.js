/*
export function someGetter (state) {
}
*/

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
