<template>
  <q-page class="bg-primary olac-font">
    <div :hidden="activePage !== 1">
      <div class="container top text-center">
        <p>The <b>Omaha Lithuanian-American Community</b> will be hosting a gala event, celebrating 70 years of
          preserving Lithuanian language, culture, customs, and traditions in Omaha. Please join us and our
          distinguished honorees.</p>

        <div class="schedule">
          <div class="schedule-inner">
            <p>
              <b class="text-h5" style="color: #d4bd9d;">The Belvedere</b><br/>
              201 East 1st Street<br/>
              Papillion, NE 68046<br/>
              <b class="text-h6" style="color: #d4bd9d;">Saturday, April 22<sup>nd</sup></b>
            </p>

            <hr/>

            <table style="width: 100%">
              <tr>
                <td class="text-left">Cocktails</td>
                <td>-</td>
                <td class="text-right">5:30pm</td>
              </tr>
              <tr>
                <td class="text-left">Opening Remarks</td>
                <td>-</td>
                <td class="text-right">6:15pm</td>
              </tr>
              <tr>
                <td class="text-left">Dinner</td>
                <td>-</td>
                <td class="text-right">6:30pm</td>
              </tr>
              <tr>
                <td class="text-left">Special Greetings</td>
                <td>-</td>
                <td class="text-right">7:15pm</td>
              </tr>
              <tr>
                <td class="text-left">Program and Entertainment</td>
                <td>-</td>
                <td class="text-right">9:00pm</td>
              </tr>
            </table>
          </div>
        </div>

        <p style="color: #475971;"><i>Cocktail attire is
          recommended.</i></p>
      </div>

      <div class="container people text-center">
        <p>Dinner will be catered by <b>Aron Mackevicius</b> and the <b>Lithuanian Bakery</b> who bring over 60 years of
          culinary experience to celebrate the Lithuanian American Community Anniversary. Owner and Executive Chef of
          <b>Talus Spirits and Sustenance</b>, <b>Aron</b> will share his flavors and techniques for us to savor and
          enjoy, including baked salmon, prime rib, potato and vegetable dishes. Little ones will enjoy delicious
          children’s options. Dessert will feature world-famous Napoleon torte.</p>

        <p>An open cash bar will be hosted by the <b>Polish American Community</b>.</p>

        <hr/>

        <p>Entertainment will be provided by<br> <span
          class="text-h6"><b>DJ Arthur Bereisa</b>, <b>Nida Grigalavičiūtė</b>, and the <b>Aušra</b> dance group.</span>
        </p>

        <hr/>

        <p><b>Nida Grigalavičiūtė</b> has been nominated for and received awards for her musical talent across a range
          of music from classical and blues to rock and jazz.</p>

        <p>The <b>Aušra</b> dance group and their supporters have been an integral part of the Omaha Lithuanian
          community for over 50 years, sharing their love of dance and the desire to preserve their culture.</p>

        <p><b>DJ Arthur Bereisa</b> will be providing well known Lithuanian music to the audience.</p>
      </div>

      <div class="rsvp container text-center">
        <p class="text-h5">Kindly RSVP below by April 8th, 2023.</p>
      </div>

      <div class="donation container text-center">
        <p>Can’t attend but would still like to make a donation? Click here:<br/><a
          href="https://www.paypal.com/donate/?hosted_button_id=N4ZY3QGD6CMVN" target="_"><img
          src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" alt="Donate Button"></a></p>
      </div>

      <div class="reservations container text-center">
        <span class="text-h5" style="color: #475971;">Ticket Reservations:</span>

        <div>
          <q-form greedy class="q-gutter-lg" @submit="onSubmit">
            <div class="row justify-center q-gutter-md">
              <q-input class="width-400" label="First Name *" v-model.trim="firstName" lazy-rules
                       :rules="[val => !!val || 'First name is required']"/>
              <q-input class="width-400" label="Last Name *" v-model.trim="lastName" lazy-rules
                       :rules="[val => !!val || 'Last name is required']"/>
              <q-input class="width-400" label="Email *" v-model.trim="email" lazy-rules
                       :rules="[val => !!val || 'Email is required', isValidEmail]"/>
              <q-input class="width-400" label="Phone" v-model="phone" mask="(###) ###-####" lazy-rules
                       :rules="[val => !val || val.length == 14 || 'Please enter your full phone number']"/>
              <q-input v-for="type in ticketTypes" :key="type.code" ref="ticketFields" class="width-400"
                       :label="type.description + ' @ ' + currency(type.costPerTicket) + ' each'"
                       v-model.number="type.count" type="number" lazy-rules :rules="[
                         val => val !== null && val !== '' || 'Ticket count must be a number',
                         val => val >= 0 || 'Must be zero or more',
                         atLeastOneTicket,
                         validateTicketsAvailable
                       ]" @focus="onTicketFieldFocus"/>
            </div>

            <div v-if="notEnoughTickets" class="error">Not enough tickets are available.</div>

            <div class="row">
              <div class="col-12 center">
                <q-btn type="submit" label="Submit"/>
              </div>
            </div>
          </q-form>
        </div>
      </div>
    </div>

    <div :hidden="activePage !== 2" class="container q-gutter-lg">
      <p>Please confirm the following information:</p>

      <div class="row justify-center q-gutter-lg">
        <div class="width-400">
          <div class="text-h5">{{ firstName }} {{ lastName }}</div>
          <div>{{ email }}</div>
          <div v-if="phone">{{ phone }}</div>
        </div>

        <div>
          <q-markup-table>
            <tr>
              <th class="text-left" scope="col">Ticket Type</th>
              <th class="text-center" scope="col">Count</th>
              <th class="text-right" scope="col">Subtotal</th>
            </tr>

            <tr v-for="type in ticketTypes.filter(t => t.count > 0)" :key="type.code">
              <td class="text-left">{{ type.description }}</td>
              <td class="text-center">{{ type.count }}</td>
              <td class="text-right">{{ currency(type.count * type.costPerTicket) }}</td>
            </tr>

            <tr>
              <td colspan="2" class="text-right text-weight-bold">Total:</td>
              <td class="text-right text-weight-bold">{{ currency(getGrandTotal()) }}</td>
            </tr>
          </q-markup-table>
        </div>
      </div>


      <div class="text-center">
        <p>Does everything look correct?</p>
      </div>

      <div class="text-center q-gutter-lg">
        <q-btn label="Yes" @click="onConfirmation"/>
        <q-btn label="No" @click="onMakeOrderChanges"/>
      </div>
    </div>

    <div :hidden="activePage !== 3" class="container q-gutter-lg">
      <p>Thank you for reserving tickets to the Omaha Lithuanian Community 70th Anniversary Event!</p>

      <p>How would you like to pay?</p>

      <div class="row justify-center">
        <div class="width-400 q-gutter-sm">
          <q-radio v-model="paymentMethod" val="online" label="Pay online now"/>
          <br/>
          <q-radio v-model="paymentMethod" val="check" label="Pay by check later"/>
        </div>
        <div class="width-400">
          <div v-if="paymentMethod === 'online'">
            <PayPalButton :purchase-units="purchaseUnits" @approved="onPayPayPaymentAccepted"/>
          </div>
          <div v-if="paymentMethod === 'check'">
            <p>Please note that your tickets will not be reserved until payment has been received, so be sure to
              send your payment by check as soon as possible to ensure your reservation for the event.</p>

            <p>If you would prefer to pay online, please select the online option on the left at this time. If you
              would still prefer to pay by check, click the button below.</p>

            <q-btn label="Pay by Check" @click="onPayByCheck"/>
          </div>
        </div>
      </div>
    </div>

    <div :hidden="activePage !== 4" class="container q-gutter-lg">
      <p>Thank you very much for supporting the Omaha Lithuanian-American Community! A confirmation has been sent to the
        email address you provided.</p>

      <p>We look forward to seeing you at our 70th Anniversary celebration on April 22nd.</p>

      <p style="margin-left: 4em;">
        <b>The Belvedere</b><br/>
        201 East 1st Street<br/>
        Papillion, NE 68046
      </p>
    </div>

    <div :hidden="activePage !== 5" class="container q-gutter-lg">
      <p>Thank you very much for your support! Please make your check in the amount of
        <b>{{ currency(getGrandTotal()) }}</b> payable to the <b>Omaha Lithuanian-American Community</b> and send it to:
      </p>

      <p style="margin-left: 4em;" th:fragment="payment-address">
        OLAC<br/>
        3515 Jefferson Street<br/>
        Omaha, NE 68107
      </p>

      <p>Be sure to include your reservation number, <b>{{ reservationNumber }}</b>, on the memo line of the check.</p>

      <p>A reminder containing this information has been sent to the email address you provided.</p>

      <p>We look forward to seeing you at our 70th Anniversary celebration on April 22nd!</p>
    </div>
  </q-page>
</template>

<script>
import {ref} from "vue";
import {api} from "boot/axios";
import {currency} from "boot/helper";
import PayPalButton from "components/PayPalButton.vue";

export default {
  name: "MainTickets",
  components: {PayPalButton},
  methods: {
    currency,
    sendGtagEvent(eventType, transactionId = null) {
      console.log(`Tracking event: ${eventType}`)

      gtag('event', eventType, {
        currency: 'USD',
        transaction_id: transactionId,
        value: this.getGrandTotal(),
        items: this.ticketTypes
          .map(t => {
            return {
              item_name: t.description,
              price: t.costPerTicket,
              quantity: t.count
            }
          })
      })
    },
    onSubmit() {
      this.sendGtagEvent('add_to_cart')
      this.sendGtagEvent('view_cart')

      this.activePage = 2
    },
    onMakeOrderChanges() {
      this.sendGtagEvent('remove_from_cart')

      this.activePage = 1
    },
    getGrandTotal() {
      return this.ticketTypes
        .map(t => t.count * t.costPerTicket)
        .reduce((a, b) => a + b, 0)
    },
    onConfirmation() {
      this.sendGtagEvent('begin_checkout')

      this.purchaseUnits = [{
        "amount": {
          "value": this.getGrandTotal(),
          "breakdown": {
            "item_total": {"value": this.getGrandTotal(), "currency_code": "USD"},
          },
          "currency_code": "USD"
        },
        "description": "Omaha Lithuanian Community's 70th Anniversary Celebration on Saturday, April 22, 2023",
        "items": this.ticketTypes
          .filter(t => t.count > 0)
          .map(t => {
            return {
              "name": t.description,
              "quantity": `${t.count}`,
              "unit_amount": {"value": t.costPerTicket, "currency_code": "USD"}
            }
          }),
        "custom_id": this.reservationId,
        "soft_descriptor": "70th Anniversary"
      }]

      this.activePage = 3
    },
    onPayPayPaymentAccepted(orderData) {
      this.sendGtagEvent('purchase', this.reservationId)

      api.post("/api/public/reservations", {
        "reservationId": this.reservationId,
        "firstName": this.firstName,
        "lastName": this.lastName,
        "email": this.email,
        "phone": this.phone,
        "ticketCounts": this.ticketTypes
          .filter(t => t.count > 0)
          .map(t => {
            return {"ticketTypeCode": t.code, "count": t.count}
          }),
        "payPayPayment": orderData
      })
        .then(response => this.activePage = 4)
    },
    onPayByCheck() {
      this.sendGtagEvent('purchase', this.reservationId)

      api.post("/api/public/reservations", {
        "reservationId": this.reservationId,
        "firstName": this.firstName,
        "lastName": this.lastName,
        "email": this.email,
        "phone": this.phone,
        "ticketCounts": this.ticketTypes
          .filter(t => t.count > 0)
          .map(t => {
            return {"ticketTypeCode": t.code, "count": t.count}
          })
      })
        .then(response => this.activePage = 5)
    },
    onTicketFieldFocus() {
      this.$refs.ticketFields.forEach(f => f.resetValidation())
    }
  },
  setup() {
    const ticketTypes = ref([])

    return {
      activePage: ref(1),
      firstName: ref(''),
      lastName: ref(''),
      email: ref(''),
      phone: ref(''),
      ticketTypes,
      notEnoughTickets: ref(false),
      paymentMethod: ref('online'),
      reservationId: ref(''),
      reservationNumber: ref(0),
      purchaseUnits: ref([]),

      isValidEmail(val) {
        const emailPattern = /^(?=[a-zA-Z0-9@._%+-]{6,254}$)[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\.){1,8}[a-zA-Z]{2,63}$/;
        return emailPattern.test(val) || 'Enter a valid email address';
      },
      validateTicketsAvailable(val) {
        let total = ticketTypes.value.reduce((sum, type) => sum = sum + +type.count, 0)
        return new Promise((resolve) => {
          api.get(`/api/public/reservations/_available?ticketCount=${total}`)
            .then(response => resolve(response.data || 'Not enough tickets are available'))
            .catch(error => resolve('Unable to verify tickets available. Try again later.'))
        })
      },
      atLeastOneTicket(val) {
        let total = ticketTypes.value.reduce((sum, type) => sum = sum + +type.count, 0)
        return total > 0 || 'You must order at least one ticket'
      }
    }
  },
  mounted() {
    api.get("/api/public/ticket-types")
      .then(response => {
        this.ticketTypes = response.data
        this.ticketTypes.forEach(t => t.count = 0)
      })

    api.get("/api/public/new-reservation-id")
      .then(response => this.reservationId = response.data)
  }
}
</script>

<style scoped>
.container {
  max-width: 1000px;
  background-color: ghostwhite;
  margin: auto;
  padding: 20px;
}

.schedule {
  max-width: 25em;
  background-color: #475971;
  color: #f8f8ff;
  padding: 5px;
  margin: auto;
}

.schedule-inner {
  border: 1px solid #d4bd9d;
  margin: 5px;
  padding: 10px;
}

.top {
  background-color: #eaebe6;
}

.people {
  background-color: #d4bd9b;
  margin-top: 10px;
}

.rsvp {
  background-color: #eaebe6;
  margin-top: 10px;
  padding-top: 3px;
  padding-bottom: 3px;
}

.donation {
  background-color: #475971;
  color: #f8f8ff;
  padding-top: 0px;
  padding-bottom: 0px;
}

.reservations {
  background-color: #eaebe6;
  margin-top: 10px;
}

.width-400 {
  width: 400px;
}

.error {
  color: darkred;
}
</style>
