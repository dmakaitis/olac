<template>
  <q-layout view="hHh lpR fFf">

    <q-header elevated class="bg-primary">
      <div class="q-mx-auto text-center">
        <img class="headerimage" src="~assets/olac-logo.svg" alt="Omaha Lithuanian-American Community"/>
      </div>

      <q-toolbar>
        <q-toolbar-title>
          <q-tabs align="center" no-caps>
            <q-route-tab to="/main/about" replace label="About Us"/>
            <q-route-tab to="/main/tickets" target="_top" label="OLAC 70th Anniversary Reservations"/>
            <q-route-tab v-if="isLoggedIn" to="/main/reservations" replace label="Reservations"/>
            <q-route-tab v-if="isAdmin" to="/admin/users" replace label="Admin"/>
          </q-tabs>
        </q-toolbar-title>

        <q-btn v-if="isLoggedIn" align="right" dense flat round icon="logout" to="/logout"/>
      </q-toolbar>

    </q-header>

    <q-footer class="bg-primary text-center">
      &copy; {{ new Date().getFullYear() }} Omaha Lithuanian-American Community
    </q-footer>

    <q-page-container>
      <router-view/>
    </q-page-container>

  </q-layout>
</template>

<script>
import {ref} from 'vue';
import {useStore} from "vuex";

export default {
  name: 'AdminLayout',
  setup() {
    return {
      isAdmin: ref(false),
      isLoggedIn: ref(false)
    }
  },
  mounted() {
    this.isAdmin = useStore().getters['auth/isAdmin']
    this.isLoggedIn = useStore().getters['auth/isLoggedIn']
  }
}
</script>

<style scoped>
.headerimage {
  padding: 10px;
  height: 150px;
}
</style>
