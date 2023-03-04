<template>
  <q-page class="bg-primary olac-font">
    <div class="container">
      <ArticleWithImages title="Omaha Lithuanian-American Community" right-images="/img/olac-2007.jpg">
        <p>For multiple generations, this organization has sustained its foundational existence to maintain Lithuanian
          consciousness that unites all Lithuanians outside Lithuanian’s borders and promotes Lithuanian culture and
          language.</p>

        <p>During the 19th and 20th centuries, the community established its roots within the framework of St Anthony’s
          Church in South Omaha. The wave of Lithuanian refugees established numerous groups such as a choir, language
          classes, scout troops, folk dancing, amateur theatrical group, athletic teams, sports club, women’s club, and
          a youth organization that provided social support.</p>

        <p>Significantly the past and present members were and still are highly actively working with Nebraska’s
          regional and state political representatives that endorse the best interests for Lithuanian as well as this
          local community. </p>

        <p>On April 26th, 1953, Omaha officially established the Omaha Lithuanian American Community Chapter that is
          part of the World Lithuanian Community. This affiliation assists to unify all 36 U.S. chapters that provides
          guidelines, support, and direction both from a social and political perspective. </p>

        <p>Our community continues its mission to keep traditions and heritage alive. Lithuanian festivities and
          historical commemorations are still celebrated. Aušra, the Omaha Folk Dance group just recently participated
          in 2022 at the 16th Lithuanian Folk Dance Festival in Philadelphia. </p>

        <p>Today, it is with honor, respect, and pride we come together to celebrate its 70th year as a formal chapter
          that has been an integral part of what makes Omaha one of the best places to live. </p>
      </ArticleWithImages>

      <q-separator/>

      <ArticleWithImages title="Aušra" :left-images="['/img/ausra-2022.jpg', '/img/ausra-old.jpg']">
        <p>Omaha's Lithuanian folk dance group, Aušra, first began as a part of the Omaha's Lithuanian Scout
          organization where Lithuanian youth fell in love with folk dancing and performed for the community. Then, in
          1971, the official dance group that still exists today was organized and founded by Gražina Reškevičienė and
          Laima Antanėlienė, in order to include all Lithuanian Americans in the Omaha area. They first performed at the
          national dance festival, Sokiu Svente, in Chicago in 1972, and have since danced all over the country and
          overseas.</p>

        <p>Today, Aušra consists of dancers of all ages. Veterans, students and youth dance together in order to connect
          with their heritage and each other. They have most recently performed at the 16th Lithuanian Folk Dance
          Festival in Philadelphia, PA in June 2022, and have plans to continue dancing long into the future and with
          future generations.</p>

        <p>Aušra continues to be a strong part of the Omaha Lithuanian community. From performing at local Lithuanian
          events, to preserving the Lithuanian language, to staying connected with the Lithuanian population still
          thriving in Omaha today, Aušra members are proud to represent their heritage and continue to live the lives
          that their relatives worked so hard to give them when they came to Omaha so many years ago.</p>
      </ArticleWithImages>

      <q-separator/>

      <ArticleWithImages title="Women's Club" left-images="/img/womens-club.jpg">
        <p>The Lithuanian women’s club was founded in 1974 to promote and sustain the Lithuanian language, culture, and
          traditions in the Omaha community.</p>

        <p>Their objectives were multifold; its mission is to support its members spiritually, protection and support of
          Lithuanian senior citizens, and to use every available opportunity to inform the public of the plight of the
          then occupied Lithuania. Women’s club reopened the Lithuanian Language Saturday school that had previously
          closed in 1967. The club protected and supported the school until it closed in 2008.</p>

        <p>The club sponsored weaving demonstrations and straw ornaments classes. The members participated in a
          exhibition of ethnic Christmas trees and other ethnic festivals. Fundraising activities to support local and
          Lithuanian charities featured Lithuanian food.</p>

        <p>Throughout the years OLWC was not only supportive financially to orphan children in Lithuania, but also
          donated money to organizations as Red Cross, Ukraine, and those in need.</p>

        <p>OLWC published their own recipe cookbook. With this cookbook, they honored and fondly remember their
          grandmothers, mothers and all of the former members who have done so much to enrich the Lithuanian legacy in
          Omaha.</p>
      </ArticleWithImages>

      <q-separator/>

      <ArticleWithImages title="Omaha Sister Cities Association (OSCA)" subtitle="Omaha Friends of Šiauliai (OFŠ)"
                         left-images="/img/ofs-2013.jpg">
        <p> The <b>Omaha Sister Cities Association (OSCA)</b> was established in 1965, with the initiation of Omaha’s
          first sister-city relationship with Shizuoka, Japan. OSCA is an all-volunteer, nonprofit organization
          promoting citizen diplomacy between Omaha and six sister cities located in Japan, Germany, Lithuania, Ireland,
          Mexico, and China. The mission of OSCA is to promote Omaha as a global city through the development
          international partnerships and networks, and the sharing best practices through citizen-to-citizen
          connections, programs, and exchanges. OSCA functions as a catalyst for connecting Omaha citizens and
          organizations with similar entities in each of our sister cities in the areas of culture, education, public
          non-governmental organizations, business, and local government.</p>

        <p>The <b>Omaha-Šiauliai Sister-City Partnership Agreement</b> was officially signed by representatives of both
          <b>Omaha</b> and <b>Šiauliai</b> city governments, the <b>Omaha Sister Cities Association</b>, and Omaha’s <b>Lithuanian-American
            Community</b> at a special ceremony at Šiauliai City Hall on May 17, 1996. With the foundational support of
          both city governments, as well as the <b>University of Nebraska – Omaha</b> and <b>Šiauliai University</b>
          (now the <b>Šiauliai Academy of Vilnius University</b>) the citizens of Omaha and Šiauliai have enjoyed a
          vibrant and colorful relationship with many highlights over the last 27+ years. The OSCA <b>‘Omaha Friends of
            Šiauliai’ (OFŠ)</b> programming committee continues to organize and support a variety of cultural and
          educational projects and initiatives, including exchanges between visiting government and citizen delegations,
          university students and faculty, as well as music, song, dance, theater and fine- arts performers and
          ensembles.</p>

        <p>The <b>OSCA-OFŠ</b> programming committee is currently engaged in a long-term milestone project – the
          development of a Lithuanian folk wood sculpture ensemble and forest pathway (<b>Saulės Takas</b>) at Omaha’s
          botanical center (<b>Lauritzen Gardens</b>), in collaboration with traditional folk artisans from Lithuania.
        </p>
      </ArticleWithImages>
    </div>
  </q-page>
</template>

<script>
import ArticleWithImages from "components/ArticleWithImages.vue";

export default {
  name: "MainAbout",
  components: {ArticleWithImages}
}
</script>

<style scoped>
.container {
  max-width: 1000px;
  background-color: ghostwhite;
  margin: auto;
  padding: 20px;
}

hr {
  clear: both
}
</style>
