<template>
  <q-layout view="hHh lpr fFf">

    <q-header elevated class="bg-primary">
      <div class="q-mx-auto text-center">
        <img class="headerimage" src="~assets/olac-logo.svg" alt="Omaha Lithuanian-American Community"/>
      </div>
      <q-tabs no-caps>
        <q-route-tab href="/" target="_top" label="About Us"/>
        <q-route-tab href="/tickets" target="_top" label="OLAC 70th Anniversary Reservations"/>
        <q-route-tab to="/reservations" replace label="Reservations"/>
        <q-route-tab to="/ticket-types" replace label="Ticket Types"/>
      </q-tabs>
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

export default {
  name: 'AdminLayout',

  // setup() {
  // const leftDrawerOpen = ref(false)
  //
  // return {
  //   leftDrawerOpen,
  //   toggleLeftDrawer() {
  //     leftDrawerOpen.value = !leftDrawerOpen.value
  //   }
  // }
  // }
}
</script>

<style scoped>
.headerimage {
  padding: 10px;
  height: 150px;
}
</style>
