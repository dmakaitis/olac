import MainLayout from "layouts/MainLayout.vue";
import AdminLayout from "layouts/AdminLayout.vue";
import AdminTicketTypes from "pages/AdminTicketTypes.vue";
import AdminReservations from "pages/AdminReservations.vue";
import AdminUsers from "pages/AdminUsers.vue";
import LoginUser from "pages/LoginUser.vue";
import LogoutUser from "pages/LogoutUser.vue";

const routes = [
  {
    name: 'login',
    path: '/login',
    component: LoginUser
  },
  {
    name: 'logout',
    path: '/logout',
    component: LogoutUser
  },
  {
    path: '/',
    redirect: {path: '/main/reservations'}
  },
  {
    path: '/main',
    component: MainLayout,
    children: [
      {
        path: 'reservations',
        component: AdminReservations,
        meta: {
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'ticket-types',
        component: AdminTicketTypes
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
