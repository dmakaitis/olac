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
                <q-form class="q-gutter-md">
                  <q-input square filled v-model="username" label="Username"/>
                  <q-input square filled v-model="password" type="password" label="Password"/>
                </q-form>
              </q-card-section>
              <q-card-actions class="q-px-md">
                <q-btn unelevated color="light-green-7" size="lg" class="full-width" label="Login" @click="onLogin"/>
              </q-card-actions>
            </q-card>
          </div>
        </div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script>
import {ref} from 'vue'
import {useStore} from 'vuex'

export default {
  name: "LoginUser",
  methods: {
    onLogin() {
      let request = {
        username: this.username,
        password: this.password
      }

      fetch("/api/auth/login", {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
      })
        .then(response => response.text())
        .then(token => {
          console.log("Received token: " + token)
          this.store.commit('auth/storeJwtToken', token)
          this.$router.push('/reservations')
        })
        .catch(err => alert(err));

      this.showDetail = false;
    }
  },
  setup() {
    const store = useStore()

    return {
      store,
      username: ref(""),
      password: ref("")
    }
  }
}
</script>

<style scoped>
.q-card {
  width: 430px;
}
</style>
