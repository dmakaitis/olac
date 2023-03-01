<template>
  <q-layout view="hhh lpr fff">
    <q-page-container>
      <q-page class="bg-primary window-width row justify-center items-center">
        <div class="column">
          <div class="row">
            <h5 class="text-h5 text-white q-my-md">Omaha Lithuanian-American Community</h5>
          </div>
          <div class="row">
            <q-card square bordered class="q-pa-lg shadow-1">
              <q-card-section>
                <GoogleSignin @auth="onAuth"/>
              </q-card-section>
            </q-card>
          </div>
        </div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script>
import {useStore} from 'vuex'
import GoogleSignin from "components/GoogleSignin.vue";

export default {
  name: "LoginUser",
  components: {GoogleSignin},
  methods: {
    onAuth(token) {
      console.log("Received token: " + token)
      this.store.commit('auth/storeJwtToken', token)
      this.$router.push('/reservations')
    }
  },
  setup() {
    const store = useStore()

    return {
      store
    }
  }
}
</script>

<style scoped>
.q-card {
  width: 430px;
}
</style>
