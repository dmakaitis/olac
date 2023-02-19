<template>
  <q-page padding class="q-gutter-md">
    <q-table title="Ticket Types" :rows="state.rows" :columns="columns" row-key="code" @row-click="onRowClick"
             selection="single" v-model:selected="selected"/>
    <q-btn label="New Ticket Type" @click="onNewTicketType"/>
    <q-btn label="Delete Selected Ticket Type" @click="onDelete"/>
  </q-page>

  <q-dialog v-model="showDetail">
    <q-card>
      <div class="q-pa-md">
        <q-card-section>
          <b>Ticket Type</b>
        </q-card-section>
        <q-card-section>
          <q-form @submit="onSave" @reset="onCancel" class="q-gutter-md">
            <q-input outlined v-model="detail.description" label="Description"/>
            <q-input outlined v-model="detail.costPerTicket" label="Cost per Ticket" prefix="$"/>
            <div>
              <q-btn label="Save" type="submit" color="primary"/>
              <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm"/>
            </div>
          </q-form>
        </q-card-section>
      </div>
    </q-card>
  </q-dialog>
  <ConfirmationDialog v-model="confirmDelete" @yes="onConfirmDelete">
    Are you sure you want to delete the ticket type '<b>{{ selected[0].description }}</b>'?
  </ConfirmationDialog>
</template>

<script>
import {reactive, ref} from 'vue'
import {currency} from "boot/helper";
import ConfirmationDialog from "components/ConfirmationDialog.vue";

const columns = [
  {
    name: 'code',
    required: false,
    label: 'Type Code',
    align: 'left',
    field: row => row.code,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'description',
    required: true,
    label: 'Description',
    align: 'left',
    field: row => row.description,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'costPerTicket',
    required: true,
    label: 'Cost per Ticket',
    align: 'right',
    field: row => row.costPerTicket,
    format: val => `${currency(val)}`,
    sortable: false
  }
];

export default {
  name: 'AdminTicketTypes',
  components: {ConfirmationDialog},
  methods: {
    onRowClick(event, row, index) {
      this.detail.code = row.code;
      this.detail.description = row.description;
      this.detail.costPerTicket = row.costPerTicket;
      this.showDetail = true;
    },
    onCancel() {
      this.showDetail = false;
    },
    onSave() {
      fetch("/api/admin/ticket-types", {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.detail)
      })
        .then(response => response.text())
        .then(text => this.loadTicketTypeData())
        .catch(err => alert(err));

      this.showDetail = false;
    },
    onDelete() {
      this.confirmDelete = true;
    },
    onConfirmDelete() {
      fetch(`/api/admin/ticket-types?code=${this.selected[0].code}`, {
        method: 'DELETE'
      })
        .then(response => response.text())
        .then(text => this.loadTicketTypeData())
        .catch(err => alert(err));
    },
    onNewTicketType() {
      this.detail.code = null;
      this.detail.description = '';
      this.detail.costPerTicket = 0.0;
      this.showDetail = true;
    },

    loadTicketTypeData() {
      fetch("/api/admin/ticket-types", {method: 'GET'})
        .then(response => response.json())
        .then(data => (this.state.rows = data))
        .catch(err => {
          console.log(err);
        });
    }
  },
  setup() {
    const state = reactive({rows: []})
    const detail = reactive({
      code: '',
      description: '',
      costPerTicket: 0.0
    })

    return {
      columns,
      state,
      showDetail: ref(false),
      detail,
      selected: ref([]),
      confirmDelete: ref(false)
    }
  },
  mounted() {
    this.loadTicketTypeData();
  }
}
</script>
