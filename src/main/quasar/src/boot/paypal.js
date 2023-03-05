import {boot} from 'quasar/wrappers'

function loadScript(url, className) {
  let isLoaded = document.querySelectorAll(className)
  if (isLoaded.length > 0) {
    // Script is already loaded
  } else {
    let myScript = document.createElement('script')
    myScript.src = url
    myScript.setAttribute('data-sdk-integration-source', 'button-factory')
    myScript.className = className
    document.body.appendChild(myScript)
  }
}

function loadPayPalLibrary(store) {
  let config = store.state.config.config
  if (config && config.payPal && !window.paypal) {
    loadScript(`https://www.paypal.com/sdk/js?client-id=${config.payPal.clientId}&enable-funding=venmo&currency=USD`, 'payPalScript')
  }
}

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async () => {
})

export {loadPayPalLibrary}
