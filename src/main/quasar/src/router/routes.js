import AdminLayout from "layouts/AdminLayout.vue";
import AdminTicketTypes from "pages/AdminTicketTypes.vue";
import AdminReservations from "pages/AdminReservations.vue";
import AdminUsers from "pages/AdminUsers.vue";

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
      },
      {
        path: 'users',
        component: AdminUsers
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
