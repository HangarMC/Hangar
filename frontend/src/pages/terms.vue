<script lang="ts" setup>
import { useHead } from "@unhead/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import Markdown from "~/components/Markdown.vue";

const i18n = useI18n();
const route = useRoute();

const text = `
Terms and Conditions
====================

Please read these terms and conditions carefully before using Our Service. By accessing or using the Service, You agree to be bound by these Terms and
Conditions. If You disagree with any part of these Terms and Conditions then You may not access the Service.

Definitions
-----------

* **We** (referred to as either "We", "Us" or "Our" in this Agreement) refers to Hangar under PaperMC.
* **Service** refers to the Website.
* **Website** refers to Hangar, accessible from [https://hangar.papermc.io/](https://hangar.papermc.io/)
  or [https://hangar.papermc.dev/](https://hangar.papermc.dev/).

Acceptable Conduct
------------------
Our [Resource Guidelines](/guidelines) of this Website and Our [Community Guidelines](https://papermc.io/community-guidelines) apply.

Privacy
-------
See Our [Privacy Policy](/privacy).

Content and Copyright
---------
Our Service allows You to post Content. You are responsible for the Content that You post to the Service, including its legality, reliability, and
appropriateness.

By posting Content to the Service, You grant Us the right and license to use, modify, publicly perform, publicly display, reproduce, and distribute such Content
on and through the Service. You retain any and all of Your rights to any Content You submit, post, or display on or through the Service and You are responsible
for protecting those rights. You agree that this license includes the right for Us to make Your Content available to other users of the Service, who may also
use Your Content subject to these Terms.

You represent and warrant that: (i) the Content is Yours (You own it) or You have the right to use it and grant Us the rights and license as provided in these
Terms, and (ii) the posting of Your Content on or through the Service does not violate the privacy rights, publicity rights, copyrights, contract rights or any
other rights of any person.

If it is reasonable to believe that Your copyrighted content is used on this Website without permission, please [contact us](#contact-us) to have it removed.


Links to Other Websites
-----------------------

Our Service may contain links to third-party web sites or services that are not owned or controlled by Us.

We have no control over, and assume no responsibility for, the content, privacy policies, or practices of any third party web sites or services. You
further acknowledge and agree that We shall not be responsible or liable, directly or indirectly, for any damage or loss caused or alleged to be caused
by or in connection with the use of or reliance on any such content, goods or services available on or through any such web sites or services.

Termination
-----------

We may terminate or suspend Your access immediately, without prior notice or liability, for any reason whatsoever, including without limitation if You breach
these Terms and Conditions.

Upon termination, Your right to use the Service will cease immediately.

Limitation of Liability
-----------------------

To the maximum extent permitted by applicable law, in no event shall We be liable for any special, incidental, indirect, or
consequential damages whatsoever (including, but not limited to, damages for loss of profits, loss of data or other information, for business interruption, for
personal injury, loss of privacy arising out of or in any way related to the use of or inability to use the Service, third-party software and/or third-party
hardware used with the Service, or otherwise in connection with any provision of these Terms), even if We have been advised of the
possibility of such damages and even if the remedy fails of its essential purpose.

Disclaimer
-------------------------------------

The Service is provided to You "AS IS" and "AS AVAILABLE". We do not make any representation or warranty of any kind, express or implied: (i)
as to the operation or availability of the Service, or the information, content, and materials or products included thereon; (ii) that the Service will be
uninterrupted or error-free; (iii) as to the accuracy, reliability, or currency of any information or content provided through the Service; or (iv) that the
Service, its servers, the content, or e-mails sent from or on behalf of Us are free of viruses, scripts, trojan horses, worms, malware, timebombs or
other harmful components.

Governing Law
-------------

The laws of the Country, excluding its conflicts of law rules, shall govern this Terms and Your use of the Service. Your use of the Application may also be
subject to other local, state, national, or international laws.

Disputes Resolution
-------------------

If You have any concern or dispute about the Service, You agree to first try to resolve the dispute informally by contacting Us.

Changes to These Terms and Conditions
-------------------------------------

We reserve the right, at Our sole discretion, to modify or replace these Terms at any time. If a revision is material, We will make reasonable efforts to provide a prior notice
to any new terms taking effect. What constitutes a material change will be determined at Our sole discretion.

By continuing to access or use Our Service after those revisions become effective, You agree to be bound by the revised terms. If You do not agree to the new
terms, in whole or in part, please stop using the website and the Service.

Contact Us
----------

If you have any questions about these Terms and Conditions, You can contact Us via email at admin@papermc.io.

Last updated: December 25, 2022
`;

useHead(useSeo(i18n.t("pages.termsTitle"), null, route, null));
</script>

<template>
  <div>
    <Markdown :raw="text"></Markdown>
  </div>
</template>
