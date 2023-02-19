import AdminLayout from "layouts/AdminLayout.vue";
import AdminTicketTypes from "pages/AdminTicketTypes.vue";
import AdminReservations from "pages/AdminReservations.vue";

const routes = [
  {
    path: '/',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirect: {path: 'reservations'}
      },
      {
        path: 'ticket-types',
        component: AdminTicketTypes
      },
      {
        path: 'reservations',
        component: AdminReservations
      }
    ]
  }

  // Always leave this as last one,
  // but you can also remove it
  // {
  //   path: '/:catchAll(.*)*',
  //   component: () => import('pages/ErrorNotFound.vue')
  // }
]

export default routes
