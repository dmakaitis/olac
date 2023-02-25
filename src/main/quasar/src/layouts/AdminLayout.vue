<template>
  <q-layout view="hHh lpR fFf">

    <q-header elevated class="bg-primary">
      <div class="q-mx-auto text-center">
        <img class="headerimage" src="~assets/olac-logo.svg" alt="Omaha Lithuanian-American Community"/>
      </div>

      <q-toolbar>
        <q-btn dense flat round icon="menu" @click="toggleDrawer()"/>
      </q-toolbar>

      <q-tabs align="left" no-caps>
        <q-route-tab href="/" target="_top" label="About Us"/>
        <q-route-tab href="/tickets" target="_top" label="OLAC 70th Anniversary Reservations"/>
        <q-route-tab to="/" replace label="Admin"/>
        <q-route-tab href="/logout" target="_top" Label="Logout"/>
      </q-tabs>
    </q-header>

    <q-drawer show-if-above v-model="drawerOpen" side="left" bordered>
      <q-list>
        <q-item clickable v-ripple to="/reservations">
          <q-item-section>Reservations</q-item-section>
        </q-item>
        <q-item clickable v-ripple to="/ticket-types">
          <q-item-section>Ticket Types</q-item-section>
        </q-item>
        <q-item clickable v-ripple to="/users">
          <q-item-section>Users</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

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

export default {
  name: 'AdminLayout',

  setup() {
    const drawerOpen = ref(true)

    return {
      drawerOpen,
      toggleDrawer() {
        drawerOpen.value = !drawerOpen.value
      }
    }
  }
}
</script>

<style scoped>
.headerimage {
  padding: 10px;
  height: 150px;
}
</style>
