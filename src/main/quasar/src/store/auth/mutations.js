export function storeAuthentication(state, authentication) {
  state.jwtToken = authentication.jwtToken || "";
  state.username = authentication.username || "anonymous";
  state.grants = authentication.grants || [];
}
