<template>
  <div id="signin-button"></div>
</template>

<script>
import {api} from 'boot/axios.js'

export default {
  name: "GoogleSignin",
  methods: {
    onLogin(credentialResponse) {
      api.post("/api/auth/google-id", credentialResponse)
        .then(response => this.$emit('auth', response.data))
    }
  },
  mounted() {
    window.google.accounts.id.initialize({
      client_id: "192542427030-lo0r4n23ecl4bl35v1rq0ejhn3gfffgj.apps.googleusercontent.com",
      callback: this.onLogin
    })
    window.google.accounts.id.renderButton(
      document.getElementById("signin-button"),
      {theme: "outline", size: "large"}
    )
  }
}
</script>

<style scoped>

</style>
