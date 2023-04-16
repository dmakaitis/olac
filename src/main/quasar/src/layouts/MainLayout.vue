<template>
  <q-layout view="hHh lpR fFf">

    <q-header reveal elevated class="bg-primary">
      <div class="q-mx-auto text-center">
        <img class="headerimage" src="~assets/olac-logo.svg" alt="Omaha Lithuanian-American Community"/>
      </div>

      <q-toolbar>
        <q-toolbar-title>
          <q-tabs align="center" no-caps>
            <q-route-tab to="/main/about" replace label="About Us"/>
            <q-route-tab to="/main/tickets" target="_top" label="OLAC 70th Anniversary Event"/>
            <q-route-tab v-if="isLoggedIn" to="/main/reservations" replace label="Reservations"/>
            <q-route-tab v-if="isAdmin" to="/admin/ticket-types" replace label="Admin"/>
          </q-tabs>
        </q-toolbar-title>

        <q-btn v-if="isLoggedIn" align="right" dense flat round icon="logout" to="/logout"/>
        <span v-if="!isLoggedIn">
          <q-btn v-if="store.getters['config/showLogin']" align="right" dense flat round icon="login" to="/login"/>
        </span>
      </q-toolbar>

    </q-header>

    <q-footer class="bg-primary text-center">
      &copy; {{ new Date().getFullYear() }} Omaha Lithuanian-American Community
    </q-footer>

    <q-page-container class="bg-primary olac-font">
      <router-view/>
    </q-page-container>

  </q-layout>
</template>

<script>
import {ref} from 'vue';
import {useStore} from "vuex";

export default {
  name: 'AdminLayout',
  methods: {
    showLogin() {
      console.log(`Should show login button: ${this.showLoginButton && !this.isLoggedIn}`)
      return this.showLoginButton && !this.isLoggedIn
    }
  },
  setup() {
    const store = useStore()

    return {
      store,
      isAdmin: ref(false),
      isLoggedIn: ref(false),
      showLoginButton: ref(false)
    }
  },
  mounted() {
    this.isAdmin = this.store.getters['auth/isAdmin']
    this.isLoggedIn = this.store.getters['auth/isLoggedIn']
    this.showLoginButton = this.store.getters['config/showLogin']
  }
}
</script>

<style scoped>
.headerimage {
  padding: 10px;
  height: 150px;
}
</style>
