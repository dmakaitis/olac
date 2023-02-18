<template>
  <q-layout view="hHh lpr fFf">

    <q-header elevated class="bg-primary">
      <q-tabs>
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
