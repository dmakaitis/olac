import {boot} from 'quasar/wrappers'

const currency = (value) => {
  const options = {style: 'currency', currency: 'USD'};
  const numberFormat = new Intl.NumberFormat('en-US', options);

  return numberFormat.format(value)
}

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ({/* app */}) => {

})

export {
  currency
}
