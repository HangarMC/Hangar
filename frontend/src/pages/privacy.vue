<script lang="ts" setup>
import { useHead } from "@unhead/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import Markdown from "~/components/Markdown.vue";

const i18n = useI18n();
const route = useRoute();

const text = `
# Privacy Policy
This Privacy Policy describes Our policies and procedures on the collection, use and disclosure of Your information when You use the Service and tells You about
Your privacy rights and how the law protects You.

We use Your Personal data to provide and improve the Service. By using the Service, You agree to the collection and use of information in accordance with this
Privacy Policy.

Definitions
-----------

* **We** (referred to as either "We", "Us" or "Our" in this Agreement) refers to Hangar under PaperMC.
* **Service** refers to the Website.
* **Website** refers to Hangar, accessible from [https://hangar.papermc.io/](https://hangar.papermc.io/)
  or [https://hangar.papermc.dev/](https://hangar.papermc.dev/).

Types of Data Collected
-----------------------

### Personal Data

While using Our Service, We may ask You to provide Us with certain personally identifiable information that can be used to contact or identify You. Personally
identifiable information may include, but is not limited to:

* Email address
* IP address
* User name
* Usage data

### Cookies

We use Cookies and similar tracking technologies to track the activity on Our Service and store certain information. Cookies can be "Persistent" or "Session"
Cookies. Persistent Cookies remain on Your personal computer or mobile device when You go offline, while Session Cookies are deleted as soon as You close Your
web browser. You can learn more about cookies [here](https://www.cookiesandyou.com/).

We use Session and Persistent Cookies for the purposes set out below:

* **Necessary / Essential Cookies:** These Cookies are essential to provide You with services available through the Website and to enable You to use some of its
  features. They help to authenticate users and prevent fraudulent use of user accounts. Without these Cookies, the services that You have asked for cannot be
  provided, and We only use these Cookies to provide You with those services.

Use of Your Personal Data
-------------------------

We may use Personal Data for the following purposes:

* **To provide and maintain our Service**, including to monitor the usage of our Service.
* **To manage Your Account:** to manage Your registration as a user of the Service. The Personal Data You provide can give You access to different
  functionalities of the Service that are available to You as a registered user.
* **To contact You:** to contact You by email regarding updates or informative communications related to the functionalities, products or contracted services,
  including the security updates, when necessary or reasonable for their implementation.
* **For other purposes**: We may use Your information for other purposes, such as data analysis, identifying usage trends, and to evaluate and improve our
  Service, products, services, and your experience.

We may share Your personal information in the following situations:

* **With other users:** when You share personal information or otherwise interact in the public areas with other users, such information may be viewed by all
  users and may be publicly distributed outside.
* **With Your consent**: We may disclose Your personal information for any other purpose with Your consent.

Delete Your Personal Data
-------------------------

You have the right to delete or request that We assist in deleting the Personal Data that We have collected about You.

Our Service may give You the ability to delete certain information about You from within the Service.

You may update, amend, or delete Your information at any time by signing in to Your Account, if you have one, and visiting the account settings section that
allows you to manage Your personal information. You may also contact Us to request access to, correct, or delete any personal information that You have provided
to Us.

Please note, however, that We may need to retain certain information when we have a legal obligation or lawful basis to do so.

Security of Your Personal Data
------------------------------

The security of Your Personal Data is important to Us, but remember that no method of transmission over the Internet, or method of electronic storage is 100%
secure. While We strive to use commercially acceptable means to protect Your Personal Data, We cannot guarantee its absolute security.

Children's Privacy
------------------

Our Service does not address anyone under the age of 13. We do not knowingly collect personally identifiable information from anyone under the age of 13. If You
are a parent or guardian and You are aware that Your child has provided Us with Personal Data, please contact Us. If We become aware that We have collected
Personal Data from anyone under the age of 13 without verification of parental consent, We take steps to remove that information from Our servers.

If We need to rely on consent as a legal basis for processing Your information and Your country requires consent from a parent, We may require Your parent's
consent before We collect and use that information.

Links to Other Websites
-----------------------

Our Service may contain links to other websites that are not operated by Us. If You click on a third party link, You will be directed to that third party's
site. We strongly advise You to review the Privacy Policy of every site You visit.

We have no control over and assume no responsibility for the content, privacy policies or practices of any third party sites or services.

Changes to this Privacy Policy
------------------------------

We may update Our Privacy Policy from time to time. We will notify You of any changes by posting the new Privacy Policy on this page.

We will let You know via email and/or a prominent notice on Our Service, prior to the change becoming effective, and update the "Last updated" date at the top of
this Privacy Policy.

You are advised to review this Privacy Policy periodically for any changes. Changes to this Privacy Policy are effective when they are posted on this page.

Contact Us
----------

If you have any questions about this Privacy Policy, You can contact Us via email at admin@papermc.io.

Last updated: December 25, 2022
`;

useHead(useSeo(i18n.t("pages.privacypolicyTitle"), null, route, null));
</script>

<template>
  <div>
    <Markdown :raw="text"></Markdown>
  </div>
</template>
