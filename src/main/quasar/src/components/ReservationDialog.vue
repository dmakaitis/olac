<template>
  <q-dialog persistent vmodel="model-value" @before-show="onBeforeShow">
    <q-card style="max-width: 1000px;">
      <q-form class="q-gutter-md" @submit="onSaveReservation" @reset="onCancel">
        <div class="q-pa-md">
          <q-card-section horizontal>
            <q-card-section class="q-gutter-md">
              <q-input readonly v-model="reservationData.id" label="Reservation Number"/>
              <q-input :readonly="!isFullEdit()" v-model.trim="reservationData.firstName" label="First Name" lazy-rules
                       :rules="[val => !!val || 'First name is required']"/>
              <q-input :readonly="!isFullEdit()" v-model.trim="reservationData.lastName" label="Last Name" lazy-rules
                       :rules="[val => !!val || 'Last name is required']"/>
              <q-input :readonly="!isFullEdit()" v-model.trim="reservationData.email" label="Email" lazy-rules
                       :rules="[val => !!val || 'Email is required', isValidEmail]"/>
              <q-input :readonly="!isFullEdit()" v-model="reservationData.phone" label="Phone" mask="(###) ###-####"
                       lazy-rules :rules="[val => !val || val.length == 14 || 'Please enter your full phone number']"/>
              <q-select :readonly="!isFullEdit()" v-model="reservationData.status" label="Status"
                        :options="statusOptions"/>
            </q-card-section>
            <q-card-section>
              <q-list bordered>
                <q-expansion-item default-opened group="reservation" label="Ticket Counts"
                                  header-class="text-primary">
                  <q-card>
                    <q-card-section>
                      <q-table title="Tickets" :rows="ticketTypeData" :columns="ticketTypeColumns"
                               row-key="code">
                        <template #body-cell-count="props">
                          <q-td :props="props">
                            <q-input :readonly="!isFullEdit()" v-model.number="props.row.count" lazy-rules
                                     :rules="[
                                       val => val !== null && val !== '' || 'Ticket count must be a number',
                                       val => val >= 0 || 'Must be zero or more'
                                     ]"/>
                          </q-td>
                        </template>
                      </q-table>
                    </q-card-section>
                  </q-card>
                </q-expansion-item>

                <q-expansion-item group="reservation" label="Payments" header-class="text-primary">
                  <q-card>
                    <q-card-section class="q-gutter-md">
                      <q-table title="Payments" :rows="reservationData.payments" :columns="paymentColumns" row-key="id"
                               @row-click="onSelectPaymentRow">
                        <template v-slot:top-right>
                          <div class="text-right">
                            Total due: {{ currency(reservationData.amountDue) }}<br/>
                            Total paid:
                            {{ currency(reservationData.payments.map(p => p.amount).reduce((a, b) => a + b, 0)) }}
                          </div>
                        </template>
                      </q-table>
                      <q-btn label="Add New Payment" @click="onAddNewPayment"/>
                    </q-card-section>
                  </q-card>
                </q-expansion-item>

                <q-expansion-item :hidden="!fullEdit" :disable="!fullEdit" group="reservation" label="History"
                                  header-class="text-primary">
                  <q-card>
                    <q-card-section>
                      <q-table title="History" :rows="auditData" :columns="auditColumns">
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
              <q-btn v-close-popup label="Cancel" type="reset" color="primary" flat class="q-ml-sm"/>
            </div>
          </q-card-section>
        </div>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script>
import {ref} from 'vue';
import {currency} from "boot/helper";
import {date} from "quasar";
import {api} from "boot/axios";
import {useStore} from "vuex";

const statusOptions = ["PENDING_PAYMENT", "RESERVED", "CHECKED_IN", "CANCELLED"]
const ticketTypeColumns = [
  {name: 'description', label: 'Ticket Type', field: row => row.description, align: 'left'},
  {
    name: 'costPerTicket',
    label: 'Cost per Ticket',
    field: row => row.costPerTicket,
    align: 'center',
    format: val => `${currency(val)}`
  },
  {name: 'count', label: 'Count', field: row => row.count, align: 'center'},
];
const paymentColumns = [
  {
    name: 'amount',
    label: 'Amount',
    field: row => row.amount,
    align: 'left',
    format: val => `${currency(val)}`,
    sortable: true
  },
  {name: 'status', label: 'Status', field: row => row.status, align: 'left', sortable: true},
  {name: 'method', label: 'Method', field: row => row.method, align: 'left', sortable: true},
  {name: 'notes', label: 'Notes', field: row => row.notes, align: 'left'},
  {name: 'enteredBy', label: 'Entered By', field: row => row.enteredBy, align: 'left', sortable: true},
];
const auditColumns = [
  {
    name: 'timestamp',
    label: 'Timestamp',
    field: row => row.timestamp,
    align: 'left',
    format: val => `${date.formatDate(val, 'MM/DD/YYYY HH:mm:ss')}`,
    sortable: true
  },
  {name: 'user', label: 'User', field: row => row.user, align: 'left', sortable: true},
  {name: 'description', label: 'Description', field: row => row.description, align: 'left', sortable: true},
];

export default {
  name: "ReservationDialog",
  methods: {
    currency,
    onBeforeShow() {
      this.reservationData = this.reservation
      this.ticketTypeData = this.ticketTypes

      if (this.fullEdit) {
        api.get(`/api/admin/reservations/${this.reservationData.reservationId}/audit`)
          .then(response => this.auditData = response.data)
          .catch(error => alert(error))
      } else {
        this.auditData = [];
      }
    },
    onSaveReservation() {
      this.$emit('save', this.reservationData)
    },
    onCancel() {
      this.$emit('cancel')
    },
    onSelectPaymentRow(event, row, index) {
      if (this.isFullEdit()) {
        let data = JSON.parse(JSON.stringify(row))
        data.index = index
        this.$emit('edit-payment', data)
      }
    },
    onAddNewPayment() {
      let data = {
        index: -1,
        amount: 0,
        method: "CHECK",
        status: "SUCCESSFUL",
        notes: ""
      }
      this.$emit('edit-payment', data)
    },
    isFullEdit() {
      return this.fullEdit || this.reservationData.id == ''
    }
  },
  props: {
    reservation: Object,
    ticketTypes: Object
  },
  setup(props) {
    return {
      modelValue: ref(false),
      reservationData: ref({}),
      ticketTypeData: ref([]),
      auditData: ref([]),
      fullEdit: ref(false),
      statusOptions,
      ticketTypeColumns,
      paymentColumns,
      auditColumns,

      isValidEmail(val) {
        const emailPattern = /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
        return emailPattern.test(val) || 'Enter a valid email address';
      }
    }
  },
  mounted() {
    this.fullEdit = useStore().getters['auth/isAdmin']
  }
}
</script>

<style scoped>

</style>
