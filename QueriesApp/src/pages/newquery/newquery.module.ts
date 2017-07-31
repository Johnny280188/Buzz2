import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { NewqueryPage } from './newquery';

@NgModule({
  declarations: [
    NewqueryPage,
  ],
  imports: [
    IonicPageModule.forChild(NewqueryPage),
  ],
  exports: [
    NewqueryPage
  ]
})
export class NewqueryPageModule {}
