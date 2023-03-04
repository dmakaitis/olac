import {boot} from 'quasar/wrappers'
import {api} from './axios'
import {loadPayPalLibrary} from "boot/paypal";

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ({app, store}) => {
  api.get('/api/public/client-config')
    .then(response => {
      console.log(`Storing config: ${JSON.stringify(response.data)}`);
      store.commit('config/storeConfig', response.data);

      loadPayPalLibrary(store)
    })
})
