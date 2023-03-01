<template>
  <q-page padding class="q-gutter-md">
    <q-table title="Users" :rows="state.rows" :columns="columns" row-key="id" @row-click="onRowClick"/>
    <q-btn label="New User" @click="onNewAccount"/>
  </q-page>

  <q-dialog persistent v-model="showDetail">
    <q-card>
      <div class="q-pa-md">
        <q-card-section>
          <b>Account</b>
        </q-card-section>

        <q-card-section>
          <q-form @submit="onSave" @reset="onCancel" class="q-gutter-md">
            <q-input outlined v-model="state.detail.username" label="Username"/>
            <q-input outlined v-model="state.detail.email" label="Email"/>
            <q-checkbox v-model="state.detail.admin" label="Administrator"/>
            <q-checkbox v-model="state.detail.enabled" label="Enabled"/>
            <div>
              <q-btn label="Save" type="submit" color="primary"/>
              <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm"/>
            </div>
          </q-form>
        </q-card-section>
      </div>
    </q-card>
  </q-dialog>
</template>

<script>
import {reactive, ref} from 'vue';
import {useStore} from 'vuex'
import {api} from "boot/axios";

const columns = [
  {
    name: 'username',
    required: true,
    label: 'Username',
    align: 'left',
    field: row => row.username,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'email',
    required: true,
    label: 'Email',
    align: 'left',
    field: row => row.email,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'admin',
    required: true,
    label: 'Administrator',
    align: 'center',
    field: row => row.admin,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'enabled',
    required: true,
    label: 'Enabled',
    align: 'center',
    field: row => row.enabled,
    format: val => `${val}`,
    sortable: false
  }
];

export default {
  name: "AdminUsers",
  methods: {
    onRowClick(event, row, index) {
      this.state.detail.id = row.id;
      this.state.detail.username = row.username;
      this.state.detail.email = row.email;
      this.state.detail.enabled = row.enabled;
      this.state.detail.admin = row.admin;
      this.showDetail = true;
    },
    onCancel() {
      this.showDetail = false;
    },
    onSave() {
      let request = {
        email: this.state.detail.email,
        enabled: this.state.detail.enabled,
        admin: this.state.detail.admin
      }

      if (this.state.detail.id) {
        api.put(`/api/admin/accounts/${this.state.detail.username}`, request)
          .then(response => this.loadAccounts())
          .catch(error => alert(error))
      } else {
        request.username = this.state.detail.username

        api.post('/api/admin/accounts', request)
          .then(response => this.loadAccounts())
          .catch(error => alert(error))
      }

      this.showDetail = false;
    },
    onNewAccount() {
      this.state.detail.id = null;
      this.state.detail.username = "";
      this.state.detail.email = "";
      this.state.detail.enabled = true;
      this.state.detail.admin = false;
      this.showDetail = true;
    },

    loadAccounts() {
      api.get('/api/admin/accounts')
        .then(response => this.state.rows = response.data)
        .catch(error => alert(error))
    }
  },
  setup() {
    const store = useStore()
    const state = reactive({
      rows: [],
      detail: {
        id: 0,
        username: "",
        admin: false
      }
    })
    return {
      store,
      state,
      showDetail: ref(false),
      columns
    }
  },
  mounted() {
    this.loadAccounts();
  }
}
</script>

<style scoped>

</style>
