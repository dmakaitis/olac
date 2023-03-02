<template>
  <q-page padding class="q-gutter-md">
    <q-table title="Reservations" :rows="state.rows" :columns="columns" row-key="reservationId" @row-click="onRowClick"
             selection="single" v-model:selected="selected">
    </q-table>
    <q-btn label="New Reservation"/>
    <q-btn label="Delete Selected Reservation"/>
  </q-page>

  <q-dialog persistent v-model="showDetail">
    <q-card style="max-width: 1000px;">
      <q-form class="q-gutter-md" @submit="onSaveReservation" @reset="onCancel">
        <div class="q-pa-md">
          <q-card-section horizontal>
            <q-card-section class="q-gutter-md">
              <q-input readonly v-model="detail.row.id" label="Reservation Number"/>
              <q-input outlined v-model="detail.row.firstName" label="First Name"/>
              <q-input outlined v-model="detail.row.lastName" label="Last Name"/>
              <q-input outlined v-model="detail.row.email" label="Email"/>
              <q-input outlined v-model="detail.row.phone" label="Phone"/>
              <q-select outlined v-model="detail.row.status" label="Status" :options="statusOptions"/>
            </q-card-section>
            <q-card-section>
              <q-list bordered>
                <q-expansion-item default-opened group="reservation" label="Ticket Counts" header-class="text-primary">
                  <q-card>
                    <q-card-section>
                      <q-table title="Tickets" :rows="state.ticketTypes" :columns="ticketTypeColumns" row-key="code">
                        <template #body-cell-count="props">
                          <q-td :props="props">
                            <q-input borderless v-model="props.row.count"/>
                          </q-td>
                        </template>
                      </q-table>
                    </q-card-section>
                  </q-card>
                </q-expansion-item>

                <q-expansion-item group="reservation" label="Payments" header-class="text-primary">
                  <q-card>
                    <q-card-section>
                      <q-table title="Payments" :rows="detail.row.payments" :columns="paymentColumns" row-key="id">
                      </q-table>
                    </q-card-section>
                  </q-card>
                </q-expansion-item>

                <q-expansion-item group="reservation" label="History" header-class="text-primary">
                  <q-card>
                    <q-card-section>
                      <q-table title="History" :rows="detail.audit" :columns="auditColumns">
                      </q-table>
                    </q-card-section>
                  </q-card>
                </q-expansion-item>
              </q-list>
            </q-card-section>
          </q-card-section>
          <q-card-section>
            <div>
              <q-btn label="Save" type="submit" color="primary"/>
              <q-btn label="Cancel" type="reset" color="primary" flat class="q-ml-sm"/>
            </div>
          </q-card-section>
        </div>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script>
import {reactive, ref} from 'vue'
import {useStore} from 'vuex'
import {currency} from "boot/helper";
import {api} from 'boot/axios.js'
import {date} from 'quasar'

const columns = [
  {
    name: 'id',
    required: false,
    label: 'Reservation Number',
    align: 'left',
    field: row => row.id,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'first-name',
    required: true,
    label: 'First Name',
    align: 'left',
    field: row => row.firstName,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'last-name',
    required: true,
    label: 'Last Name',
    align: 'left',
    field: row => row.lastName,
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
    name: 'phone',
    required: true,
    label: 'Phone',
    align: 'left',
    field: row => row.phone,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'status',
    required: true,
    label: 'Status',
    align: 'left',
    field: row => row.status,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'tickets',
    required: true,
    label: 'Tickets',
    align: 'center',
    field: row => row.ticketCounts,
    format: val => `${val.reduce((a, b) => a + b.count, 0)}`,
    sortable: false
  },
  {
    name: 'amount-due',
    required: true,
    label: 'Amount Due',
    align: 'right',
    field: row => row.amountDue,
    format: val => `${currency(val)}`,
    sortable: false
  },
  {
    name: 'amount-paid',
    required: true,
    label: 'Amount Due',
    align: 'right',
    field: row => row.payments,
    format: val => `${currency(val.reduce((a, b) => a + b.amount, 0.0))}`,
    sortable: false
  }
];

const ticketTypeColumns = [
  {
    name: 'description',
    required: false,
    label: 'Ticket Type',
    align: 'left',
    field: row => row.description,
    format: val => `${val}`,
    sortable: false
  },
  {
    name: 'costPerTicket',
    required: false,
    label: 'Cost per Ticket',
    align: 'center',
    field: row => row.costPerTicket,
    format: val => `${currency(val)}`,
    sortable: false
  },
  {
    name: 'count',
    required: false,
    label: 'Count',
    align: 'center',
    field: row => row.count,
    format: val => `${val}`,
    sortable: false
  },
];

const paymentColumns = [
  {
    name: 'id',
    required: false,
    label: 'ID',
    align: 'left',
    field: row => row.id,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'amount',
    required: false,
    label: 'Amount',
    align: 'left',
    field: row => row.amount,
    format: val => `${currency(val)}`,
    sortable: true
  },
  {
    name: 'status',
    required: false,
    label: 'Status',
    align: 'left',
    field: row => row.status,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'method',
    required: false,
    label: 'Method',
    align: 'left',
    field: row => row.method,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'notes',
    required: false,
    label: 'Notes',
    align: 'left',
    field: row => row.notes,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'enteredBy',
    required: false,
    label: 'Entered By',
    align: 'left',
    field: row => row.enteredBy,
    format: val => `${val}`,
    sortable: true
  },
];

const auditColumns = [
  {
    name: 'timestamp',
    required: false,
    label: 'Timestamp',
    align: 'left',
    field: row => row.timestamp,
    format: val => `${date.formatDate(val, 'MM/DD/YYYY HH:mm:ss')}`,
    sortable: true
  },
  {
    name: 'user',
    required: false,
    label: 'User',
    align: 'left',
    field: row => row.user,
    format: val => `${val}`,
    sortable: true
  },
  {
    name: 'description',
    required: false,
    label: 'Description',
    align: 'left',
    field: row => row.description,
    format: val => `${val}`,
    sortable: true
  },
];

export default {
  name: 'AdminTicketTypes',
  methods: {
    onRowClick(event, row, index) {
      this.detail.row = row;

      this.state.ticketTypes.forEach(type => {
        type.count = 0;

        let count = this.detail.row.ticketCounts.find(c => c.ticketTypeCode === type.code)
        if (count) {
          type.count = count.count;
        }
      });

      api.get(`/api/admin/reservations/${this.detail.row.reservationId}/audit`)
        .then(response => this.detail.audit = response.data)
        .catch(error => alert(error))
      this.showDetail = true;
    },
    onCancel() {
      this.showDetail = false;
    },
    loadReservations() {
      api.get('/api/admin/reservations')
        .then(response => this.state.rows = response.data)
        .catch(error => alert(error))
    },
    loadTicketTypeData() {
      api.get('/api/admin/ticket-types')
        .then(response => {
          this.state.ticketTypes = response.data;
          this.state.ticketTypes.forEach(type => type.count = 0)
        })
        .catch(error => alert(error))
    },
    onSaveReservation() {
      this.detail.row.ticketCounts = this.state.ticketTypes
        .filter(t => t.count > 0)
        .map(t => {
          return {
            ticketTypeCode: t.code,
            count: parseInt(t.count)
          }
        });
      this.detail.row.amountDue = this.state.ticketTypes
        .map(t => t.count * t.costPerTicket)
        .reduce((t, n) => t + n);

      console.log(`Saving reservation: ${JSON.stringify(this.detail.row)}`);
      api.put(`/api/admin/reservations/${this.detail.row.reservationId}`, this.detail.row)
        .then(response => this.loadReservations())
        .catch(error => alert(error));

      this.showDetail = false;
    }
  },
  setup() {
    const store = useStore()
    const state = reactive({rows: []})

    return {
      store,
      columns,
      ticketTypeColumns,
      paymentColumns,
      auditColumns,
      statusOptions: [
        "PENDING_PAYMENT", "RESERVED", "CHECKED_IN", "CANCELLED"
      ],
      state,
      showDetail: ref(false),
      detail: reactive({}),
      selected: ref([])
    }
  },
  mounted() {
    this.loadReservations();
    this.loadTicketTypeData();
  }
}
</script>
